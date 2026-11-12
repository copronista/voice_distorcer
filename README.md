# Voice Distorcer

Native Android app (Kotlin) that anonymizes voice on the device before sharing it with messaging apps.

The repository maintains two variants:

- **Full** (`main`): records audio or video, anonymizes the voice, and shares the result.
- **Minimal** (`audio_only`): imports or receives audio only, anonymizes it, and shares it. It has no microphone, camera, or video support and keeps external dependencies to a minimum.

## Features

- Import audio with the system document picker.
- Receive audio through `ACTION_VIEW` or `ACTION_SEND`.
- On-device voice processing with pitch and formant shifting plus adjustable effects.
- Presets including chipmunk, monster, kid, old man, stutter, reverse, alien, drunk, ghost, telephone, megaphone, and bee.
- Editable effect parameters, cleanup controls, and expert DSP settings.
- Audio preview before sharing.
- Share through the Android Sharesheet using a `FileProvider` `content://` URI.
- Settings export and import.
- No network access and no audio uploads.

The minimal variant does not request `RECORD_AUDIO` or `CAMERA` permissions and does not include camera, video recording, video import, or video processing code.

## Requirements

- Android 7.0 or newer (`minSdk 24`)
- Android SDK 35
- JDK 17

## Build

```sh
./gradlew assembleDebug
```

Release build:

```sh
./gradlew assembleRelease
```

Run unit tests and lint:

```sh
./gradlew testDebugUnitTest lintDebug
```

## Usage

1. Open Voice Distorcer.
2. Record a message in the full variant, or select/share an audio file in the minimal variant.
3. Adjust the voice settings if needed.
4. Preview the anonymized clip.
5. Share it with an installed messaging app.

## Packages

- `processing`: decoding, DSP, encoding, and voice anonymization.
- `playback`: audio preview.
- `settings`: presets, persisted settings, and settings backup.
- `sharing`: Android Sharesheet and `FileProvider` integration.
- `ui`: Jetpack Compose screens and view models.

## Release variants

Every release must include both the full build and the minimal build. The minimal variant uses the `minimal` suffix for its release tag, release title, and APK filename.

- Full package: `com.voicedistorcer`
- Minimal package: `com.voicedistorcer.audio`

See [AGENTS.md](AGENTS.md) for the release policy and repository constraints.
