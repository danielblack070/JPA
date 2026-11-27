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
import com.ace.jp.app.data.model.WordList

@Composable
fun AddWordListDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetFolderId: Int?) -> Unit,
    existingNames: List<String> = emptyList(),
    initialName: String = "",
    initialFolderId: Int? = null,
    editingListId: Int? = null,
    allFolders: List<Folder> = emptyList(),
    allWordLists: List<WordList> = emptyList(),
    isEditMode: Boolean = false
) {
    var name by remember { mutableStateOf(initialName) }
    var targetFolderId by remember { mutableStateOf(initialFolderId) }
    var locationExpanded by remember { mutableStateOf(false) }

    val moveOptions = remember(allFolders, initialFolderId, isEditMode) {
        if (!isEditMode) emptyList() else buildMoveFolderOptions(
            allFolders = allFolders,
            currentParentId = initialFolderId,
            excludeFolderId = null
        )
    }

    val effectiveExistingNames = remember(allWordLists, existingNames, targetFolderId, editingListId, isEditMode) {
        if (isEditMode && allWordLists.isNotEmpty()) {
            allWordLists
                .filter { it.folderId == targetFolderId && it.id != editingListId }
                .map { it.name }
        } else {
            existingNames
        }
    }

    val isBlank = name.trim().isBlank()
    val isDuplicate = effectiveExistingNames.any {
        it.equals(name.trim(), ignoreCase = true) &&
                (!isEditMode || !it.equals(initialName.trim(), ignoreCase = true) || targetFolderId != initialFolderId)
    }
    val isError = isBlank || isDuplicate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (!isEditMode && initialName.isEmpty()) "Add Word List" else "Edit Word List") },
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
                    val currentLabel = moveOptions.firstOrNull { it.first == targetFolderId }?.second ?: "Root"
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
                                        targetFolderId = folderId
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
                onClick = { if (!isError) onConfirm(name.trim(), targetFolderId) },
                enabled = !isError
            ) { Text("Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}