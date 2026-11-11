package com.voicedistorcer.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicedistorcer.R
import com.voicedistorcer.recording.VideoRecordingController
import com.voicedistorcer.ui.theme.VoiceDistorcerTheme
import java.util.Locale

class VideoActivity : ComponentActivity() {
    private val viewModel: VideoViewModel by viewModels()

    private val controller by lazy {
        VideoRecordingController(
            context = this,
            lifecycleOwner = this,
            onCameraError = viewModel::onCameraError,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceDistorcerTheme {
                VideoMessageScreen(
                    viewModel = viewModel,
                    controller = controller,
                    onClose = { finish() },
                )
            }
        }
    }

    override fun onStop() {
        val state = viewModel.uiState.value
        if (state is VideoUiState.Recording || state is VideoUiState.Starting) {
            controller.stopRecording()
        }
        super.onStop()
    }

    override fun onDestroy() {
        controller.release()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoMessageScreen(
    viewModel: VideoViewModel,
    controller: VideoRecordingController,
    onClose: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermissions by remember { mutableStateOf(context.hasVideoPermissions()) }
    var startAfterGrant by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state !is VideoUiState.Ready) {
            showPreview = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        hasPermissions = context.hasVideoPermissions()
        if (!hasPermissions) {
            startAfterGrant = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermissions = context.hasVideoPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val startRecording: () -> Unit = {
        viewModel.onRecordingStarting()
        val outputFile = viewModel.newRecordingFile()
        controller.startRecording(
            outputFile = outputFile,
            onStarted = viewModel::onRecordingStarted,
            onFinalized = viewModel::onRecordingFinalized,
            onError = viewModel::onRecordingError,
        )
    }

    LaunchedEffect(hasPermissions, startAfterGrant) {
        if (hasPermissions && startAfterGrant) {
            startAfterGrant = false
            startRecording()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.video_title)) },
                actions = {
                    TextButton(onClick = onClose) {
                        Text(stringResource(R.string.video_close))
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
                text = stringResource(R.string.video_intro),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            VideoStatusCard(state)

            if (!hasPermissions) {
                VideoPermissionCard(
                    onGrant = {
                        startAfterGrant = true
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                            ),
                        )
                    },
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                CameraPreview(
                    controller = controller,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Text(
                    text = stringResource(R.string.video_settings_note),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }

            if (state is VideoUiState.Ready) {
                OutlinedButton(
                    onClick = { showPreview = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.video_preview))
                }

                Button(
                    onClick = {
                        if (!viewModel.share(context)) {
                            Toast.makeText(
                                context,
                                R.string.error_video_share_failed,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.video_share))
                }
            }

            Button(
                onClick = {
                    when (state) {
                        is VideoUiState.Recording -> controller.stopRecording()
                        is VideoUiState.Processing, is VideoUiState.Starting -> Unit
                        else -> {
                            if (hasPermissions) {
                                startRecording()
                            } else {
                                startAfterGrant = true
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.RECORD_AUDIO,
                                    ),
                                )
                            }
                        }
                    }
                },
                enabled = state !is VideoUiState.Processing && state !is VideoUiState.Starting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        when (state) {
                            is VideoUiState.Recording -> R.string.video_stop
                            is VideoUiState.Ready, is VideoUiState.Error ->
                                R.string.video_record_again
                            else -> R.string.video_record
                        },
                    ),
                )
            }
        }
    }

    val readyState = state as? VideoUiState.Ready
    if (showPreview && readyState != null) {
        VideoPreviewDialog(
            file = readyState.file,
            onDismiss = { showPreview = false },
        )
    }
}

@Composable
private fun CameraPreview(
    controller: VideoRecordingController,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                controller.bind(this)
            }
        },
    )
}

@Composable
private fun VideoStatusCard(state: VideoUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (state) {
                VideoUiState.Idle -> Text(
                    text = stringResource(R.string.video_status_idle),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                VideoUiState.Starting -> Text(
                    text = stringResource(R.string.video_status_starting),
                    style = MaterialTheme.typography.titleMedium,
                )
                is VideoUiState.Recording -> {
                    Text(
                        text = stringResource(R.string.video_status_recording),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatVideoDuration(state.elapsedMs),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                VideoUiState.Processing -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = stringResource(R.string.video_status_processing),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                is VideoUiState.Ready -> {
                    Text(
                        text = stringResource(R.string.video_status_ready),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatVideoDuration(state.durationMs),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                is VideoUiState.Error -> Text(
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
private fun VideoPermissionCard(onGrant: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.video_permission_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.video_permission_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.video_grant_permission))
            }
        }
    }
}

private fun formatVideoDuration(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

private fun Context.hasVideoPermissions(): Boolean =
    hasPermission(Manifest.permission.CAMERA) &&
        hasPermission(Manifest.permission.RECORD_AUDIO)

private fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
