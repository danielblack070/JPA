package com.ace.jp.app.ui.collection.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.jp.app.data.model.Type
import com.ace.jp.app.data.model.SubRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubRuleDialog(
    existingSubRules: List<SubRule> = emptyList(),
    editingSubRuleId: Int? = null,
    initialOriginalEnding: String = "",
    initialNewEnding: String = "",
    initialType: Type = Type.Noun,
    initialIsUnique: Boolean = false,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Type, String?, String, Boolean) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var originalEnding by remember { mutableStateOf(initialOriginalEnding) }
    var newEnding by remember { mutableStateOf(initialNewEnding) }
    var isUnique by remember { mutableStateOf(initialIsUnique) }

    var typeExpanded by remember { mutableStateOf(false) }
    var helpDialogText by remember { mutableStateOf<String?>(null) }

    // Automatic ending filling & locking based on Type
    val isEndingEnabled = selectedType != Type.Noun && selectedType != Type.IAdjective && selectedType != Type.NaAdjective && selectedType != Type.IrrAdjective
    val isUniqueAvailable = selectedType == Type.UVerb || selectedType == Type.RuVerb

    LaunchedEffect(selectedType) {
        originalEnding = when (selectedType) {
            Type.Noun -> ""
            Type.IAdjective -> "い"
            Type.NaAdjective -> "な"
            Type.IrrAdjective -> "いい"
            else -> if (isEditMode && selectedType == initialType) initialOriginalEnding else ""
        }
        if (!isUniqueAvailable) {
            isUnique = false
        }
    }

    val activeOriginalEnding = if (isEndingEnabled) originalEnding else when (selectedType) {
        Type.Noun -> ""
        Type.IAdjective -> "い"
        Type.NaAdjective -> "な"
        Type.IrrAdjective -> "いい"
        else -> ""
    }

    // Uniqueness validation within same type under the active master rule
    val isUniqueCheckPassed = if (isUniqueAvailable && activeOriginalEnding.isNotBlank()) {
        existingSubRules.none { sub ->
            (editingSubRuleId == null || sub.id != editingSubRuleId) &&
                    sub.type == selectedType &&
                    sub.originalEnding?.trim().orEmpty().equals(activeOriginalEnding.trim(), ignoreCase = true)
        }
    } else {
        true
    }

    val isFormValid = (!isEndingEnabled || activeOriginalEnding.trim().isNotBlank()) &&
            isUniqueCheckPassed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Rule" else "Add Rule") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Select Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType.toDisplayString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { typeExpanded = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Type.entries.filter { it != Type.Other }.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.toDisplayString()) },
                                onClick = {
                                    selectedType = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Original Ending Field with optional question help
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = activeOriginalEnding,
                        onValueChange = { if (isEndingEnabled) originalEnding = it },
                        label = { Text("Original Ending") },
                        singleLine = true,
                        enabled = isEndingEnabled,
                        isError = !isUniqueCheckPassed,
                        supportingText = {
                            if (!isUniqueCheckPassed) {
                                Text("Must be unique within the same type.", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            helpDialogText = if (isUnique) {
                                "Exact word (and it's compounds) to create a unique rule for"
                            } else {
                                "Final kana(s) of the dictionary form to be replaced"
                            }
                        }
                    ) {
                        Text("❓", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // New Ending Field with optional question help
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newEnding,
                        onValueChange = { newEnding = it },
                        label = { Text("New Ending") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            helpDialogText = if (isUnique) {
                                "The conjugated form of the word and it's compounds"
                            } else {
                                "Replacement of the removed final kana(s) plus the additional kana(s) for the conjugation form"
                            }
                        }
                    ) {
                        Text("❓", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Is Unique Checkbox and Help Info Trigger (visible when not locked)
                if (isUniqueAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isUnique = !isUnique }
                        ) {
                            Checkbox(checked = isUnique, onCheckedChange = { isUnique = it })
                            Text("Is Unique")
                        }
                        IconButton(
                            onClick = {
                                helpDialogText = "If the conjugation of a word differs from what their final kana(s) would suggest, enable this to create a unique rule for the word and it's compounds"
                            }
                        ) {
                            Text("❓", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onConfirm(
                            selectedType,
                            if (activeOriginalEnding.isBlank()) null else activeOriginalEnding.trim(),
                            newEnding.trim(),
                            if (isUniqueAvailable) isUnique else false
                        )
                    }
                },
                enabled = isFormValid
            ) { Text("Submit") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    // Help Dialog Overlay
    if (helpDialogText != null) {
        AlertDialog(
            onDismissRequest = { helpDialogText = null },
            title = { Text("Helper Explanation") },
            text = { Text(helpDialogText!!, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = { helpDialogText = null }) { Text("Understood") }
            }
        )
    }
}
