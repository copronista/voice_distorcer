package com.voicedistorcer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
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
    onEchoChanged: (Double) -> Unit,
    onFanChanged: (Double) -> Unit,
    onAdvancedSettingsChanged: (VoiceSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val funPresets = listOf(
        VoicePreset.CHIPMUNK to R.string.preset_chipmunk,
        VoicePreset.MONSTER to R.string.preset_monster,
        VoicePreset.KID to R.string.preset_kid,
        VoicePreset.OLD_MAN to R.string.preset_old_man,
        VoicePreset.STUTTER to R.string.preset_stutter,
        VoicePreset.REVERSE to R.string.preset_reverse,
        VoicePreset.ALIEN to R.string.preset_alien,
        VoicePreset.DRUNK to R.string.preset_drunk,
        VoicePreset.GHOST to R.string.preset_ghost,
        VoicePreset.TELEPHONE to R.string.preset_telephone,
        VoicePreset.MEGAPHONE to R.string.preset_megaphone,
        VoicePreset.BEE to R.string.preset_bee,
    )
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

                Text(
                    text = stringResource(R.string.settings_fun_presets_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    funPresets.forEach { (preset, labelRes) ->
                        PresetChip(
                            label = stringResource(labelRes),
                            selected = settings.preset == preset,
                            onClick = { onPresetSelected(preset) },
                        )
                    }
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

                Text(
                    text = stringResource(R.string.settings_effects_label),
                    style = MaterialTheme.typography.titleSmall,
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

                SettingSlider(
                    label = stringResource(
                        R.string.settings_echo_value,
                        formatPercent(settings.echo),
                    ),
                    value = settings.echo,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = onEchoChanged,
                )

                SettingSlider(
                    label = stringResource(
                        R.string.settings_fan_value,
                        formatPercent(settings.fan),
                    ),
                    value = settings.fan,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = onFanChanged,
                )

                Text(
                    text = stringResource(R.string.settings_fun_effects_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_stutter_value,
                        formatPercent(settings.stutter),
                    ),
                    value = settings.stutter,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withStutter(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_alien_value,
                        formatPercent(settings.alien),
                    ),
                    value = settings.alien,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withAlien(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_drunk_value,
                        formatPercent(settings.drunk),
                    ),
                    value = settings.drunk,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withDrunk(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_telephone_value,
                        formatPercent(settings.telephone),
                    ),
                    value = settings.telephone,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withTelephone(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_megaphone_value,
                        formatPercent(settings.megaphone),
                    ),
                    value = settings.megaphone,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withMegaphone(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_bee_value,
                        formatPercent(settings.bee),
                    ),
                    value = settings.bee,
                    valueRange = VoiceSettings.MIN_EFFECT.toFloat()..VoiceSettings.MAX_EFFECT.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withBee(it))
                    },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.reverse,
                        onClick = {
                            onAdvancedSettingsChanged(settings.withReverse(!settings.reverse))
                        },
                        label = { Text(stringResource(R.string.settings_reverse_label)) },
                    )
                }

                Text(
                    text = stringResource(R.string.settings_cleanup_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                SettingSlider(
                    label = if (settings.highPassHz < 1.0) {
                        stringResource(R.string.settings_high_pass_off)
                    } else {
                        stringResource(
                            R.string.settings_high_pass_value,
                            formatInteger(settings.highPassHz),
                        )
                    },
                    value = settings.highPassHz,
                    valueRange = VoiceSettings.MIN_HIGH_PASS_HZ.toFloat()..
                        VoiceSettings.MAX_HIGH_PASS_HZ.toFloat(),
                    steps = HIGH_PASS_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withHighPass(it))
                    },
                )
                SettingSlider(
                    label = if (settings.noiseGateDb <= VoiceSettings.MIN_NOISE_GATE_DB) {
                        stringResource(R.string.settings_noise_gate_off)
                    } else {
                        stringResource(
                            R.string.settings_noise_gate_value,
                            formatSignedInteger(settings.noiseGateDb),
                        )
                    },
                    value = settings.noiseGateDb,
                    valueRange = VoiceSettings.MIN_NOISE_GATE_DB.toFloat()..
                        VoiceSettings.MAX_NOISE_GATE_DB.toFloat(),
                    steps = NOISE_GATE_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withNoiseGate(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_compressor_value,
                        formatPercent(settings.compressor),
                    ),
                    value = settings.compressor,
                    valueRange = VoiceSettings.MIN_COMPRESSOR.toFloat()..
                        VoiceSettings.MAX_COMPRESSOR.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withCompressor(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_output_gain_value,
                        formatSigned(settings.outputGainDb),
                    ),
                    value = settings.outputGainDb,
                    valueRange = VoiceSettings.MIN_OUTPUT_GAIN_DB.toFloat()..
                        VoiceSettings.MAX_OUTPUT_GAIN_DB.toFloat(),
                    steps = OUTPUT_GAIN_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withOutputGain(it))
                    },
                )

                Text(
                    text = stringResource(R.string.settings_shaping_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_formant_mix_value,
                        formatPercent(settings.formantMix),
                    ),
                    value = settings.formantMix,
                    valueRange = VoiceSettings.MIN_FORMANT_MIX.toFloat()..
                        VoiceSettings.MAX_FORMANT_MIX.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withFormantMix(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_smoothing_value,
                        formatPercent(settings.spectralSmoothing),
                    ),
                    value = settings.spectralSmoothing,
                    valueRange = VoiceSettings.MIN_SPECTRAL_SMOOTHING.toFloat()..
                        VoiceSettings.MAX_SPECTRAL_SMOOTHING.toFloat(),
                    steps = EFFECT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withSpectralSmoothing(it))
                    },
                )
                SettingSlider(
                    label = stringResource(
                        R.string.settings_formant_gain_value,
                        formatOneDecimal(settings.formantGainLimit),
                    ),
                    value = settings.formantGainLimit,
                    valueRange = VoiceSettings.MIN_FORMANT_GAIN_LIMIT.toFloat()..
                        VoiceSettings.MAX_FORMANT_GAIN_LIMIT.toFloat(),
                    steps = GAIN_LIMIT_STEPS,
                    onValueChange = {
                        onAdvancedSettingsChanged(settings.withFormantGainLimit(it))
                    },
                )

                Text(
                    text = stringResource(R.string.settings_expert_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                ChoiceRow(
                    label = stringResource(R.string.settings_formant_fft_label),
                    options = VoiceSettings.FFT_SIZES,
                    selected = settings.formantFftSize,
                    format = { it.toString() },
                    onSelected = {
                        onAdvancedSettingsChanged(settings.withFormantFftSize(it))
                    },
                )
                ChoiceRow(
                    label = stringResource(R.string.settings_formant_overlap_label),
                    options = VoiceSettings.FORMANT_OVERLAPS,
                    selected = settings.formantOverlap,
                    format = { "${it}x" },
                    onSelected = {
                        onAdvancedSettingsChanged(settings.withFormantOverlap(it))
                    },
                )
                ChoiceRow(
                    label = stringResource(R.string.settings_pitch_fft_label),
                    options = VoiceSettings.FFT_SIZES,
                    selected = settings.pitchFftSize,
                    format = { it.toString() },
                    onSelected = {
                        onAdvancedSettingsChanged(settings.withPitchFftSize(it))
                    },
                )
                ChoiceRow(
                    label = stringResource(R.string.settings_pitch_overlap_label),
                    options = VoiceSettings.PITCH_OVERLAPS,
                    selected = settings.pitchOverlap,
                    format = { "${it}x" },
                    onSelected = {
                        onAdvancedSettingsChanged(settings.withPitchOverlap(it))
                    },
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
private fun ChoiceRow(
    label: String,
    options: List<Int>,
    selected: Int,
    format: (Int) -> String,
    onSelected: (Int) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = { Text(format(option)) },
            )
        }
    }
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

private fun formatInteger(value: Double): String = String.format(Locale.US, "%.0f", value)

private fun formatSignedInteger(value: Double): String =
    String.format(Locale.US, "%+.0f", value)

private fun formatOneDecimal(value: Double): String =
    String.format(Locale.US, "%.1f", value)

private fun formatPercent(value: Double): String =
    String.format(Locale.US, "%.0f%%", value.coerceIn(0.0, 1.0) * 100.0)

private const val TONE_STEPS = 23
private const val EFFECT_STEPS = 9
private const val HIGH_PASS_STEPS = 29
private const val NOISE_GATE_STEPS = 11
private const val OUTPUT_GAIN_STEPS = 23
private const val GAIN_LIMIT_STEPS = 4
