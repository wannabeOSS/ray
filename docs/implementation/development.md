# Android development guide

For full laptop/emulator, phone installation and feature-by-feature testing steps, see [the testing guide](testing.md).

## Pinned baseline

Kotlin 2.2.21 · Android Gradle Plugin 8.13.2 · Gradle 8.13 · JDK 17 · Compose BOM 2025.12.00 · compile/target SDK 36 · minimum SDK 29. Versions are deliberate stable pins, not a claim to use the newest release. The official [AGP compatibility table](https://developer.android.com/build/releases/agp-8-13-0-release-notes) supports Gradle 8.13/JDK 17; the [Compose release](https://developer.android.com/blog/posts/whats-new-in-the-jetpack-compose-december-release) documents this BOM.

## Build

Install JDK 17 and Android SDK platform 36/build tools 35.0.0, then set `JAVA_HOME` and `sdk.dir` in ignored `local.properties`. The checked-in Gradle wrapper verifies its distribution checksum. On the current machine, use the already prepared local environment:

```sh
source .local/build-env.sh
./gradlew :core:domain:test :app:assembleDebug :app:lintDebug
make docs-check
```

`.local/build-env.sh` and SDK/license/cache files are local and ignored; recreate your environment on another machine. System Java 26 is not the selected Gradle runtime. The JDK archive, Android command-line archive and Gradle distribution were checked against publisher checksums. SDK platform/build packages were downloaded by Google’s SDK installer. Do not check in toolchains, signing material or local SDK paths.

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`. With a user-selected connected development device:

```sh
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n dev.androidisland/.MainActivity
```

Do not install to an arbitrary connected device without identifying the intended target. Release signing/distribution is not configured.

## Manual smoke sequence

1. Open the app with all optional access denied. Search an installed app and launch it.
2. Enter `2+3*4`, `10 km to mi`, and `y=sin(x*2)*5,y2=floor(x)`. Copy a result.
3. Edit an app: favorite, hide/show, add alias, restart and check persisted preferences.
4. Open Emoji, search “hand”, copy an emoji. Open Clipboard and explicitly Import while focused; test pause/delete/clear and lock clearing.
5. Expand the in-app island and run/cancel the labeled demo. No AI request occurs.
6. Request torch control only on a flash-equipped phone; test denial and camera contention.
7. Enable media access deliberately, start a player and check only advertised playback controls.
8. Grant overlay access, return to the app and tap Start. With active media, torch or demo, observe the floating island. Test stop, lock, landscape, revoke and process death.

The overlay is generic for privacy and opens the focused palette for details. It is a feasibility implementation; notification/charging integration and the production reducer actor are not wired yet. Current app UI is English; RTL/text scale/TalkBack and physical-device evidence remain open tasks.
