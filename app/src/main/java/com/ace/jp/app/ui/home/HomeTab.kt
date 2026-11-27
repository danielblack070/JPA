package com.ace.jp.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ace.jp.app.data.SettingsManager

private enum class HomeSubScreen {
    Main, Settings, Help
}

@Composable
fun HomeTabContent(settingsManager: SettingsManager) {
    var currentScreen by remember { mutableStateOf(HomeSubScreen.Main) }

    when (currentScreen) {
        HomeSubScreen.Main -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { currentScreen = HomeSubScreen.Settings },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButton(
                    onClick = { currentScreen = HomeSubScreen.Help },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Help")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Help", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        HomeSubScreen.Settings -> {
            SettingsScreen(
                settingsManager = settingsManager,
                onBack = { currentScreen = HomeSubScreen.Main }
            )
        }

        HomeSubScreen.Help -> {
            HelpScreen(onBack = { currentScreen = HomeSubScreen.Main })
        }
    }
}

@Composable
private fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    val settings by settingsManager.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. General Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "General",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                    SettingToggleRow(
                        title = "Katakana-Free Mode",
                        description = "Display Katakana meanings as original *hiragana* (keeping ー unchanged) and accept Hiragana for Katakana in typing practice.",
                        checked = settings.katakanaFreeMode,
                        onCheckedChange = { settingsManager.setKatakanaFreeMode(it) }
                    )
                }
            }

            // 2. Collection Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Collection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                    Text(
                        text = "No collection settings available yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Practice Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Practice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                    SettingToggleRow(
                        title = "All Meanings",
                        description = "Require entering every meaning of the word in vocabulary typing practice instead of only one.",
                        checked = settings.allMeaningsRequired,
                        onCheckedChange = { settingsManager.setAllMeaningsRequired(it) }
                    )
                    HorizontalDivider()
                    SettingToggleRow(
                        title = "Senses Required",
                        description = "Require entering the sense for every typed meaning that has a sense.",
                        checked = settings.sensesRequired,
                        onCheckedChange = { settingsManager.setSensesRequired(it) }
                    )
                    HorizontalDivider()
                    SettingToggleRow(
                        title = "Dual Mode",
                        description = "Require pronunciation in vocabulary typing answers (when it exists), even when Kanji Mode is enabled.",
                        checked = settings.dualMode,
                        onCheckedChange = { settingsManager.setDualMode(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun HelpScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Help",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HelpSectionCard(
                title = "Kanji Mode",
                body = "Toggle Kanji Mode in the Add/Edit Word dialog (Japanese meanings). When Kanji Mode is ON, pronunciation is hidden in prompts and Text is required in typing practice. When Kanji Mode is OFF, pronunciation is shown and required."
            )
            HelpSectionCard(
                title = "Katakana-Free Mode",
                body = "Available under Settings -> General. Displays Katakana meanings as original *hiragana* (keeping ー unchanged) and lets you answer Katakana questions using Hiragana in typing practice."
            )
            HelpSectionCard(
                title = "Vocabulary Typing Practice",
                body = "Use + to add multiple meaning answers, ← and → to switch between your answers, and X to remove a meaning. Every filled field must match the same target meaning."
            )
            HelpSectionCard(
                title = "Practice Settings",
                body = "• All Meanings: Every meaning of the word must be answered.\n• Senses Required: Sense must be filled whenever the target meaning has one.\n• Dual Mode: Pronunciation is also required when Kanji Mode is ON (if a pronunciation exists)."
            )
        }
    }
}

@Composable
private fun HelpSectionCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}