# AGENTS.md

## What this project is
Native Android app (Kotlin) that:
1. Records a voice message
2. Distorts the voice so the speaker is anonymized
3. Shares the anonymized message with the messaging apps installed on the device

## Hard constraints (do not skip)
- Write ALL code, comments, and commit messages in English, even though the maintainer communicates in Italian.
- This is a greenfield repo: no Gradle/Kotlin sources exist yet. Do not assume a specific min SDK, DI framework, or audio library exists until it is actually present. If a decision is missing, ask the user rather than inventing it.
- The share step MUST go through the Android Sharesheet (`Intent.ACTION_SEND` with a `content://` URI served by a `FileProvider`). External messaging apps must be able to read the file, so never share with `file://` or raw absolute storage paths.
- Voice anonymization is the core feature, not a nice-to-have: the recorded audio must be processed (e.g. pitch/formant shifting) before sharing. Do not ship a version that shares the unprocessed clip.

## Release policy (do not skip)
- Every release must ship BOTH variants: the full build (audio + video) and the audio-only build (no video support).
- The audio-only variant must exclude camera/video dependencies and features to minimize external libraries.
- The audio-only variant must not request microphone or camera permissions and must only support importing or receiving audio files.
- Use the `minimal` suffix for the audio-only release tag, release title, and APK filename; do not use `audio` as the suffix.
- Keep versioning and release notes aligned across both variants.

## Expected wiring (agreed plan)
- Recording: Android microphone APIs (`MediaRecorder` or `AudioRecord`) → requires `RECORD_AUDIO` runtime permission plus manifest declaration.
- Processing: read the recorded file, apply distortion, write an anonymized output file.
- Sharing: `FileProvider` configured with an XML `res/xml/file_paths.xml` and a matching `<provider>` in the manifest; launch `Intent.createChooser` with `ACTION_SEND`, `TYPE: "audio/*"`, and an extra `EXTRA_STREAM` content URI.

## Platform notes agents commonly get wrong
- Runtime permission flow: `RECORD_AUDIO` is a dangerous permission — request at runtime before starting `MediaRecorder`, and handle the denial path (don't crash).
- Files exposed for sharing must live in app-specific storage (e.g. `getExternalFilesDir()` or `cacheDir`) covered by the `FileProvider` `file_paths` configuration.
- Don't manage thread/record state on the main thread for anything blocking; keep recording/processing off the UI thread.