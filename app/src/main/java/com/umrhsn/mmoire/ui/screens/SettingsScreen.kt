package com.umrhsn.mmoire.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.AppTheme
import com.umrhsn.mmoire.ui.components.AppHeader
import com.umrhsn.mmoire.ui.components.AppHeaderIcon
import com.umrhsn.mmoire.viewmodels.SettingsViewModel
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.AlertTriangle
import compose.icons.evaicons.outline.ArrowBack
import compose.icons.evaicons.outline.CheckmarkCircle2
import compose.icons.evaicons.outline.ColorPalette
import compose.icons.evaicons.outline.Flash
import compose.icons.evaicons.outline.Globe2
import compose.icons.evaicons.outline.Layout
import compose.icons.evaicons.outline.Monitor
import compose.icons.evaicons.outline.Moon
import compose.icons.evaicons.outline.Music
import compose.icons.evaicons.outline.Navigation2
import compose.icons.evaicons.outline.Settings
import compose.icons.evaicons.outline.Sun
import compose.icons.evaicons.outline.VolumeOff
import compose.icons.evaicons.outline.VolumeUp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppHeader(
            title = stringResource(R.string.app_settings),
            navigationIcon = {
                AppHeaderIcon(
                    icon = EvaIcons.Outline.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    onClick = onBackClicked
                )
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Audio Section
            SettingsCategory(
                title = stringResource(R.string.audio_settings),
                icon = EvaIcons.Outline.Music
            ) {
                SettingsToggle(
                    title = stringResource(R.string.sound_enabled),
                    checked = uiState.currentSoundEnabled,
                    onCheckedChange = { viewModel.toggleSound(it) },
                    icon = if (uiState.currentSoundEnabled) EvaIcons.Outline.VolumeUp else EvaIcons.Outline.VolumeOff
                )
            }

            // Personalization Section
            SettingsCategory(
                title = stringResource(R.string.personalization),
                icon = EvaIcons.Outline.ColorPalette
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Theme Sub-section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.theme),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        val themes = listOf(
                            AppTheme.SYSTEM to stringResource(R.string.theme_system) to EvaIcons.Outline.Layout,
                            AppTheme.LIGHT to stringResource(R.string.theme_light) to EvaIcons.Outline.Sun,
                            AppTheme.DARK to stringResource(R.string.theme_dark) to EvaIcons.Outline.Moon
                        )

                        themes.forEach { (themeData, icon) ->
                            val (theme, label) = themeData
                            SettingsOption(
                                label = label,
                                selected = uiState.currentTheme == theme,
                                onClick = { viewModel.updateTheme(theme) },
                                icon = icon
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    // Language Sub-section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.language),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        val languages = listOf(
                            null to stringResource(R.string.lang_system) to "🌐",
                            "ar-EG" to stringResource(R.string.lang_ar_eg) to "🇪🇬",
                            "ar" to stringResource(R.string.lang_ar) to "🇸🇦",
                            "en" to stringResource(R.string.lang_en) to "🇺🇸",
                            "fr" to stringResource(R.string.lang_fr) to "🇫🇷",
                            "de" to stringResource(R.string.lang_de) to "🇩🇪",
                            "es" to stringResource(R.string.lang_es) to "🇪🇸"
                        )

                        languages.forEach { (langData, emoji) ->
                            val (tag, label) = langData
                            SettingsOption(
                                label = label,
                                selected = uiState.currentLanguage == tag,
                                onClick = { viewModel.updateLanguage(tag) },
                                emoji = emoji
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Apply Button Area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .padding(bottom = 8.dp) // Extra padding for system nav bar if not edge-to-edge
            ) {
                Button(
                    onClick = { viewModel.applyChanges() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.hasChanges,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.apply_changes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategory(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun LanguageFlag(
    emoji: String,
    selected: Boolean
) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(
            alpha = 0.05f
        ),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = emoji,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 2.dp) // Offset emoji vertical alignment
            )
        }
    }
}

@Composable
private fun SettingsOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    emoji: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent,
        border = if (selected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            if (icon != null) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.05f
                    ),
                    contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else if (emoji != null) {
                LanguageFlag(emoji = emoji, selected = selected)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (selected) {
                Icon(
                    imageVector = EvaIcons.Outline.CheckmarkCircle2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
