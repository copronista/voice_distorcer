# Initial Development Plan

Native Android app (Kotlin): record a voice message, distort/anonymize the voice, and share the anonymized clip with the messaging apps installed on the device.

## Milestones

### M0 — Decisions & scaffolding
- Confirm the open decisions below with the maintainer.
- Scaffold a single-module Gradle project (`:app`), set Kotlin/Android plugin versions, create a minimal manifest.
- **Verify:** `./gradlew assembleDebug` succeeds and a blank screen launches.

### M1 — Record
- Declare `RECORD_AUDIO` in the manifest and request it at runtime; handle the denial path (show explanation, don't crash).
- Capture 16-bit PCM, mono, 44.1 kHz using `AudioRecord` into a raw PCM file (record off the UI thread).
- UI: start/stop recording button, elapsed-time indicator.
- **Verify:** recorded PCM decodes and plays without distortion/glitches.

### M2 — Anonymization (core feature)
- DSP engine that transforms the raw PCM into an anonymized clip: pitch/formant shifting so the speaker is not recognizable.
- Write the processed output to a separate file; keep processing off the UI thread with progress feedback and cancellation.
- Unit-test the DSP with a known input → expected output.
- **Verify:** output is intelligible but the speaker's identity is masked; duration is preserved.

### M3 — Encode & share
- Encode the processed PCM into a messaging-app-friendly container (see open decisions).
- Configure `FileProvider`: `res/xml/file_paths.xml` covering the app-specific storage dirs, `<provider>` entry in the manifest.
- Share via `Intent.createChooser(ACTION_SEND)`, `TYPE: "audio/*"`, `EXTRA_STREAM = content://…` only.
- **Verify:** share to real apps (Telegram/WhatsApp/Signal) and the recipient can play the anonymized clip.

### M4 — Polish & hardening
- Error/empty states, mic-permission re-request, cleanup of intermediate temp files, short/empty clip edge cases.
- Instrumented test for the permission-denial flow, unit tests for the processor.
- `./gradlew lint` clean.
- **Verify:** full loop on emulator and a physical device.

## Proposed architecture
- Packages by feature: `recording`, `processing`, `sharing`, `ui`.
- Thin UI; `RecordingEngine`, `VoiceDistortionProcessor`, `ShareGateway` as separate units.
- All blocking work (record read, DSP, encode) off the main thread via coroutines.
- Intermediate files in `cacheDir`/`getExternalFilesDir()` — always covered by `FileProvider`.

## Open decisions (need maintainer confirmation)
| Decision | Recommended | Why |
|---|---|---|
| minSdk / targetSdk | minSdk 24, targetSdk 35 | Broad device coverage; no legacy permission workarounds |
| UI toolkit | Jetpack Compose | Modern, less boilerplate; single-activity |
| Distortion algorithm | Custom DSP (phase-vocoder style pitch + formant shift) | No third-party audio dependency, full control; alternatively use an existing library if DSP expertise is out of scope |
| Output format | M4A (AAC-LC) via MediaCodec + MediaMuxer | Best playback compatibility across messaging apps; WAV is the trivial fallback if encoding is deferred |
| Anonymization strength | Pitch + formant shift (not resample-only) | Resample-only changes speed/duration and preserves speaker traits |