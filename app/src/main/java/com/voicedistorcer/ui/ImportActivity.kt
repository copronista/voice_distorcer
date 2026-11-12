package com.voicedistorcer.ui

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voicedistorcer.R
import com.voicedistorcer.ui.theme.VoiceDistorcerTheme
import java.util.Locale

class ImportActivity : ComponentActivity() {
    private val viewModel: ImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceDistorcerTheme {
                ImportScreen(
                    viewModel = viewModel,
                    onClose = { finish() },
                )
            }
        }

        if (savedInstanceState == null || viewModel.uiState.value is ImportUiState.Idle) {
            handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onStop() {
        viewModel.stopPreview()
        super.onStop()
    }

    private fun handleIntent(intent: Intent) {
        val uri = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data ?: intent.clipData.firstUri()
            Intent.ACTION_SEND -> runCatching {
                IntentCompat.getParcelableExtra(
                    intent,
                    Intent.EXTRA_STREAM,
                    Uri::class.java,
                )
            }.getOrNull() ?: intent.clipData.firstUri()
            else -> null
        }

        if (uri == null) {
            viewModel.onMissingInput()
            return
        }
        viewModel.import(uri, intent.type)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportScreen(
    viewModel: ImportViewModel,
    onClose: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.previewErrors.collect {
            Toast.makeText(context, R.string.error_preview_failed, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                actions = {
                    TextButton(onClick = onClose) {
                        Text(stringResource(R.string.close))
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
            val readyState = state as? ImportUiState.Ready

            Text(
                text = stringResource(R.string.import_intro),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            ImportStatusCard(state)

            Spacer(modifier = Modifier.weight(1f))

            if (readyState != null) {
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

            Text(
                text = stringResource(R.string.import_note),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ImportStatusCard(state: ImportUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (state) {
                ImportUiState.Idle -> Text(
                    text = stringResource(R.string.import_status_idle),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                ImportUiState.Processing -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = stringResource(R.string.import_status_processing),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                is ImportUiState.Ready -> {
                    Text(
                        text = stringResource(R.string.import_status_ready),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatImportDuration(state.durationMs),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                is ImportUiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun ClipData?.firstUri(): Uri? =
    this?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri

private fun formatImportDuration(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}
