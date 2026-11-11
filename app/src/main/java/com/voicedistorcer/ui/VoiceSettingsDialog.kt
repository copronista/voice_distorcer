package com.voicedistorcer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    onFormantChanged: (Double) -> Unit,
    onSpeedChanged: (Double) -> Unit,
    onGritChanged: (Double) -> Unit,
    onRobotChanged: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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

                SettingSlider(
                    label = stringResource(
                        R.string.settings_tone_value,
                        formatSigned(settings.toneSemitones),
                    ),
                    value = settings.toneSemitones,
                    valueRange = VoiceSettings.MIN_TONE.toFloat()..VoiceSettings.MAX_TONE.toFloat(),
                    steps = TONE_STEPS,
                    onValueChange = onToneChanged,
                )

                SettingSlider(
                    label = stringResource(
                        R.string.settings_formant_value,
                        formatSigned(settings.formantSemitones),
                    ),
                    value = settings.formantSemitones,
                    valueRange = VoiceSettings.MIN_TONE.toFloat()..VoiceSettings.MAX_TONE.toFloat(),
                    steps = TONE_STEPS,
                    onValueChange = onFormantChanged,
                )

                SettingSlider(
                    label = stringResource(
                        R.string.settings_speed_value,
                        String.format(Locale.US, "%.2f", settings.speed),
                    ),
                    value = settings.speed,
                    valueRange = VoiceSettings.MIN_SPEED.toFloat()..VoiceSettings.MAX_SPEED.toFloat(),
                    steps = 0,
                    onValueChange = onSpeedChanged,
                )

                SettingSlider(
                    label = stringResource(
                        R.string.settings_grit_value,
                        formatPercent(settings.grit),
                    ),
                    value = settings.grit,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = onGritChanged,
                )

                SettingSlider(
                    label = stringResource(
                        R.string.settings_robot_value,
                        formatPercent(settings.robot),
                    ),
                    value = settings.robot,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = onRobotChanged,
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
private fun SettingSlider(
    label: String,
    value: Double,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Double) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
    )
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.toDouble()) },
        valueRange = valueRange,
        steps = steps,
        modifier = Modifier.fillMaxWidth(),
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

private fun formatSigned(value: Double): String = String.format(Locale.US, "%+.1f", value)

private fun formatPercent(value: Double): String =
    String.format(Locale.US, "%.0f%%", value.coerceIn(0.0, 1.0) * 100.0)

private const val TONE_STEPS = 23
private const val EFFECT_STEPS = 9
