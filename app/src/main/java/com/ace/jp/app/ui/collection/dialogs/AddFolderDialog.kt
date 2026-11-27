package com.ace.jp.app.ui.collection.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.jp.app.data.model.Folder

@Composable
fun AddFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetParentFolderId: Int?) -> Unit,
    initialName: String = "",
    initialParentFolderId: Int? = null,
    editingFolderId: Int? = null,
    allFolders: List<Folder> = emptyList(),
    isEditMode: Boolean = false
) {
    var name by remember { mutableStateOf(initialName) }
    var targetParentFolderId by remember { mutableStateOf(initialParentFolderId) }
    var locationExpanded by remember { mutableStateOf(false) }

    val moveOptions = remember(allFolders, initialParentFolderId, editingFolderId, isEditMode) {
        if (!isEditMode) emptyList() else buildMoveFolderOptions(
            allFolders = allFolders,
            currentParentId = initialParentFolderId,
            excludeFolderId = editingFolderId
        )
    }

    val existingSiblingNames = remember(allFolders, targetParentFolderId, editingFolderId) {
        allFolders
            .filter { it.parentFolderId == targetParentFolderId && it.id != editingFolderId }
            .map { it.name }
    }

    val isBlank = name.trim().isBlank()
    val isDuplicate = existingSiblingNames.any { it.equals(name.trim(), ignoreCase = true) }
    val isError = isBlank || isDuplicate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (!isEditMode) "Add Folder" else "Edit Folder") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    isError = isError,
                    singleLine = true,
                    supportingText = {
                        if (isDuplicate) {
                            Text("Name must be unique in this folder", color = MaterialTheme.colorScheme.error)
                        } else if (isBlank) {
                            Text("Name is mandatory", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isEditMode && moveOptions.isNotEmpty()) {
                    val currentLabel = moveOptions.firstOrNull { it.first == targetParentFolderId }?.second ?: "Root"
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Location Folder") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Folder") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { locationExpanded = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        DropdownMenu(
                            expanded = locationExpanded,
                            onDismissRequest = { locationExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            moveOptions.forEach { (folderId, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        targetParentFolderId = folderId
                                        locationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isError) onConfirm(name.trim(), targetParentFolderId) },
                enabled = !isError
            ) { Text("Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun buildMoveFolderOptions(
    allFolders: List<Folder>,
    currentParentId: Int?,
    excludeFolderId: Int? = null
): List<Pair<Int?, String>> {
    val options = mutableListOf<Pair<Int?, String>>()
    val currentParent = currentParentId?.let { id -> allFolders.firstOrNull { it.id == id } }

    // 1. Stay in current folder
    options.add(currentParentId to (currentParent?.let { "${it.name} (Current)" } ?: "Root (Current)"))

    // 2. Move one layer outside (if not already at root)
    if (currentParentId != null) {
        val outerId = currentParent?.parentFolderId
        val outerFolder = outerId?.let { id -> allFolders.firstOrNull { it.id == id } }
        val outerLabel = if (outerFolder != null) "${outerFolder.name} (One layer up)" else "Root (One layer up)"
        options.add(outerId to outerLabel)
    }

    // 3. Move into a sibling folder within the same folder
    allFolders
        .filter { it.parentFolderId == currentParentId && it.id != excludeFolderId }
        .forEach { sibling ->
            options.add(sibling.id to "Inside: ${sibling.name}")
        }

    return options.distinctBy { it.first }
}