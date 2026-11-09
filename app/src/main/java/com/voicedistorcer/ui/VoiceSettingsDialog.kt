package com.voicedistorcer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.voicedistorcer.R
import com.voicedistorcer.settings.VoicePreset
import com.voicedistorcer.settings.VoiceSettings
import java.util.Locale

@Composable
fun VoiceSettingsDialog(
    settings: VoiceSettings,
    onPresetSelected: (VoicePreset) -> Unit,
    onToneChanged: (Double) -> Unit,
    onSpeedChanged: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.settings_preset_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetChip(
                        label = stringResource(R.string.preset_default),
                        selected = settings.preset == VoicePreset.DEFAULT,
                        onClick = { onPresetSelected(VoicePreset.DEFAULT) },
                    )
                    PresetChip(
                        label = stringResource(R.string.preset_male),
                        selected = settings.preset == VoicePreset.MALE,
                        onClick = { onPresetSelected(VoicePreset.MALE) },
                    )
                    PresetChip(
                        label = stringResource(R.string.preset_female),
                        selected = settings.preset == VoicePreset.FEMALE,
                        onClick = { onPresetSelected(VoicePreset.FEMALE) },
                    )
                }

                Text(
                    text = stringResource(
                        R.string.settings_tone_value,
                        String.format(Locale.US, "%+.1f", settings.toneSemitones),
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
                Slider(
                    value = settings.toneSemitones.toFloat(),
                    onValueChange = { onToneChanged(it.toDouble()) },
                    valueRange = VoiceSettings.MIN_TONE.toFloat()..VoiceSettings.MAX_TONE.toFloat(),
                    steps = 23,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(
                        R.string.settings_speed_value,
                        String.format(Locale.US, "%.2f", settings.speed),
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
                Slider(
                    value = settings.speed.toFloat(),
                    onValueChange = { onSpeedChanged(it.toDouble()) },
                    valueRange = VoiceSettings.MIN_SPEED.toFloat()..VoiceSettings.MAX_SPEED.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.settings_anonymization_note),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_done))
            }
        },
    )
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}
