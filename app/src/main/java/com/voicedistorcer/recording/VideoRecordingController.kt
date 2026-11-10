package com.voicedistorcer.recording

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File

class VideoRecordingController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onCameraError: (String) -> Unit,
) {
    private val executor = ContextCompat.getMainExecutor(context)

    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var previewView: PreviewView? = null
    private var recording: Recording? = null
    private var bindingRequested = false

    @Volatile
    private var released = false

    fun bind(previewView: PreviewView) {
        if (this.previewView === previewView && videoCapture != null) return
        this.previewView = previewView
        if (released) return

        cameraProvider?.let { provider ->
            bindToCamera(provider, previewView)
            return
        }

        if (bindingRequested) return
        bindingRequested = true

        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                bindingRequested = false
                if (released) return@addListener

                val target = this.previewView ?: return@addListener
                try {
                    bindToCamera(future.get(), target)
                } catch (error: Exception) {
                    onCameraError(error.message ?: "Could not start the camera")
                }
            },
            executor,
        )
    }

    @SuppressLint("MissingPermission")
    fun startRecording(
        outputFile: File,
        onStarted: () -> Unit,
        onFinalized: (File) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (recording != null) {
            onError("A recording is already in progress")
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            onError("Microphone permission is required to record video with audio")
            return
        }

        val capture = videoCapture
        if (capture == null) {
            onError("Camera is not ready")
            return
        }

        try {
            outputFile.parentFile?.mkdirs()
            val pending = capture.output.prepareRecording(
                context,
                FileOutputOptions.Builder(outputFile).build(),
            )
            val active = pending.withAudioEnabled().start(executor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> onStarted()
                    is VideoRecordEvent.Finalize -> {
                        recording = null
                        if (event.hasError()) {
                            outputFile.delete()
                            onError(event.cause?.message ?: "Video recording failed")
                        } else {
                            onFinalized(outputFile)
                        }
                    }
                    else -> Unit
                }
            }
            recording = active
        } catch (error: Exception) {
            outputFile.delete()
            onError(error.message ?: "Could not start video recording")
        }
    }

    fun stopRecording() {
        recording?.stop()
    }

    fun release() {
        released = true
        recording?.close()
        recording = null
        cameraProvider?.unbindAll()
        cameraProvider = null
        videoCapture = null
        previewView = null
    }

    @SuppressLint("MissingPermission")
    private fun bindToCamera(provider: ProcessCameraProvider, previewView: PreviewView) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.fromOrderedList(
                    listOf(Quality.FHD, Quality.HD, Quality.SD),
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD),
                ),
            )
            .build()
        val capture = VideoCapture.withOutput(recorder)
        val cameraSelector = runCatching {
            if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        }.getOrDefault(CameraSelector.DEFAULT_BACK_CAMERA)

        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            capture,
        )
        cameraProvider = provider
        videoCapture = capture
    }
}
