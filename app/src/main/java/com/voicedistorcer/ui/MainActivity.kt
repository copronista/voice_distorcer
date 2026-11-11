package com.voicedistorcer.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicedistorcer.R
import com.voicedistorcer.ui.theme.VoiceDistorcerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceDistorcerTheme {
                VoiceDistorcerScreen(viewModel)
            }
        }
    }

    override fun onStop() {
        viewModel.stopPreview()
        super.onStop()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceDistorcerScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(context.hasRecordAudioPermission()) }
    var startAfterGrant by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        if (granted && startAfterGrant) {
            viewModel.startRecording()
        }
        startAfterGrant = false
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            context.startActivity(
                Intent(context, ImportActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = uri
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = context.hasRecordAudioPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.previewErrors.collect {
            Toast.makeText(context, R.string.error_preview_failed, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    TextButton(onClick = { showSettings = true }) {
                        Text(stringResource(R.string.settings))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.intro),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            StatusCard(state)

            if (!hasPermission) {
                PermissionCard(
                    onGrant = {
                        startAfterGrant = true
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (state is UiState.Ready) {
                OutlinedButton(
                    onClick = viewModel::togglePreview,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(
                            if (isPreviewPlaying) R.string.stop_preview else R.string.preview,
                        ),
                    )
                }

                Button(
                    onClick = {
                        if (!viewModel.share(context)) {
                            Toast.makeText(
                                context,
                                R.string.error_share_failed,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.share))
                }
            }

            Button(
                onClick = {
                    when (state) {
                        is UiState.Recording -> viewModel.stopRecording()
                        is UiState.Processing -> Unit
                        else -> {
                            if (hasPermission) {
                                viewModel.startRecording()
                            } else {
                                startAfterGrant = true
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                },
                enabled = state !is UiState.Processing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        if (state is UiState.Recording) R.string.stop else R.string.record,
                    ),
                )
            }

            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(context, VideoActivity::class.java))
                },
                enabled = state !is UiState.Recording && state !is UiState.Processing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.record_video))
            }

            OutlinedButton(
                onClick = {
                    mediaPicker.launch(arrayOf("audio/*", "video/*"))
                },
                enabled = state !is UiState.Recording && state !is UiState.Processing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.open_media))
            }
        }
    }

    if (showSettings) {
        VoiceSettingsDialog(
            settings = settings,
            onPresetSelected = viewModel::applyPreset,
            onToneChanged = viewModel::updateTone,
            onFormantChanged = viewModel::updateFormant,
            onSpeedChanged = viewModel::updateSpeed,
            onGritChanged = viewModel::updateGrit,
            onRobotChanged = viewModel::updateRobot,
            onEchoChanged = viewModel::updateEcho,
            onFanChanged = viewModel::updateFan,
            onAdvancedSettingsChanged = viewModel::updateAdvancedSettings,
            onDismiss = { showSettings = false },
        )
    }
}

@Composable
private fun StatusCard(state: UiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (state) {
                UiState.Idle -> Text(
                    text = stringResource(R.string.status_idle),
                    style = MaterialTheme.typography.titleMedium,
                )
                is UiState.Recording -> {
                    Text(
                        text = stringResource(R.string.status_recording),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatDuration(state.elapsedMs),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                UiState.Processing -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = stringResource(R.string.status_processing),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                is UiState.Ready -> {
                    Text(
                        text = stringResource(R.string.status_ready),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatDuration(state.durationMs),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                is UiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.permission_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.permission_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

private fun Context.hasRecordAudioPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
