package com.ace.jp.japanesepractice.ui.collection.dialogs

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
import com.ace.jp.japanesepractice.data.model.Type
import com.ace.jp.japanesepractice.data.model.SubRule

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
    val isEndingEnabled = selectedType != Type.Noun && selectedType != Type.IAdjective && selectedType != Type.NaAdjective
    val isEndingMandatory = selectedType == Type.IrrAdjective || selectedType == Type.UVerb || selectedType == Type.RuVerb || selectedType == Type.IrrVerb

    LaunchedEffect(selectedType) {
        originalEnding = when (selectedType) {
            Type.Noun -> ""
            Type.IAdjective -> "い"
            Type.NaAdjective -> "な"
            else -> if (isEditMode && selectedType == initialType) initialOriginalEnding else ""
        }
        if (!isEndingMandatory) {
            isUnique = false
        }
    }

    val activeOriginalEnding = if (isEndingEnabled) originalEnding else when (selectedType) {
        Type.Noun -> ""
        Type.IAdjective -> "い"
        Type.NaAdjective -> "な"
        else -> ""
    }

    // Uniqueness validation within same type under the active master rule
    val isUniqueCheckPassed = if (isEndingMandatory && activeOriginalEnding.isNotBlank()) {
        existingSubRules.none { sub ->
            (editingSubRuleId == null || sub.id != editingSubRuleId) &&
                    sub.type == selectedType &&
                    sub.originalEnding?.trim().orEmpty().equals(activeOriginalEnding.trim(), ignoreCase = true)
        }
    } else {
        true
    }

    val isFormValid = newEnding.trim().isNotBlank() &&
            (!isEndingMandatory || activeOriginalEnding.trim().isNotBlank()) &&
            isUniqueCheckPassed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Edit Subrule" else "Add Subrule") },
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
                        Type.entries.filter { it != Type.Adverb && it != Type.Other }.forEach { t ->
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
                        label = { Text("Original Ending" + if (isEndingMandatory) " (Mandatory)" else "") },
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
                                "Exact word or final part of a compound to be replaced"
                            } else {
                                "Final kana(s) of the dictionary form"
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
                        label = { Text("New Ending (Mandatory)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            helpDialogText = if (isUnique) {
                                "Exact replacement of the original word or final part of the compound to replace with. Kanji will not be replaced, even if it's reading changes."
                            } else {
                                "Replacing the original final kana(s) are these"
                            }
                        }
                    ) {
                        Text("❓", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Is Unique Checkbox and Help Info Trigger (visible when not locked)
                if (isEndingMandatory) {
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
                                helpDialogText = "If the conjugation of a word differs from what their final kana(s) would suggest, enable this to create a unique rule for the word. In this case, use the entire word as original ending (or if it is a compound, the final word of the compound). Make sure the new ending also has the entire word. The new ending field will determine the reading, but the kanji in the word will not be changed."
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
                            if (isEndingMandatory) isUnique else false
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
