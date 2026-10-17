# Testing on a laptop and an Android phone

Task: PLAN-018 · Checked: 2026-10-17

This guide is for the current development APK. Laptop testing means running Android in an emulator; phone testing means installing the same APK on a physical phone. The app requires Android 10 / API 29 or higher. Android 16 / API 36 is the first suggested emulator image because this build targets API 36; it is not the only supported runtime by configuration.

Compilation, twelve JVM tests and lint have passed. No emulator or physical-phone behavior has yet been verified. Treat the expected outcomes below as tests to run, not previously established results.

## 1. What is already available on this Mac

- Project: `/Users/abhijit/Developer/Codex`
- APK: `app/build/outputs/apk/debug/app-debug.apk` (about 33 MB)
- Project-local JDK 17, Android SDK platform 36, build tools and adb
- Build environment: `.local/build-env.sh`
- No Android Emulator package/system image installed in the project-local SDK at this check

The terminal commands below are for this Mac's zsh shell. A fresh terminal needs the environment loaded again:

```sh
cd /Users/abhijit/Developer/Codex
source .local/build-env.sh
java -version
adb version
```

Java should report 17. No rebuild is needed merely to install the existing APK.

## 2. Laptop automated checks without a virtual phone

```sh
cd /Users/abhijit/Developer/Codex
source .local/build-env.sh
./gradlew :core:domain:test :app:assembleDebug :app:lintDebug
make docs-check
```

Expected: `BUILD SUCCESSFUL` and a passing documentation report. This checks domain logic, compilation and lint; it does not open a UI or exercise real hardware. Twelve tests currently cover arithmetic safety/precedence, graph gaps, conversions, search ranking, agent revisions, expiry, privacy selection and bounded storage.

Reports after the commands:

- JVM test HTML: `core/domain/build/reports/tests/test/index.html`
- Android lint HTML: `app/build/reports/lint-results-debug.html`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

The current lint run has zero errors and eleven recommendations to update pinned dependency/toolchain versions. For this machine's earlier networking timeout, the successful verification used `JAVA_TOOL_OPTIONS='-Djava.net.preferIPv4Stack=true'` before the Gradle command. If necessary, repeat the build with that prefix; do not change the project SDK/JDK versions randomly.

## 3. Laptop visual testing with Android Studio

### Create a virtual phone

1. Install [Android Studio](https://developer.android.com/studio) for **Mac with Apple silicon**; this machine is arm64.
2. Open Android Studio. Use **More Actions → SDK Manager**, or open the project and find Android SDK settings.
3. To reuse the SDK already installed for this project, set the SDK location to `/Users/abhijit/Developer/Codex/.local/toolchains/android-sdk`. Another SDK location also works, but its emulator/platform tools must be configured consistently and can duplicate downloads.
4. In SDK Tools, install **Android Emulator** and ensure **Android SDK Platform-Tools** is available.
5. Open **More Actions → Virtual Device Manager** from the welcome screen, or **View → Tool Windows → Device Manager** with a project open.
6. Choose **Create Virtual Device**, a Pixel phone profile, then an **API 36 arm64-v8a** system image, preferably Google APIs. Download it if prompted. Additional images/storage are needed beyond the current toolchain.
7. Finish the device configuration and click its play/start button. Wait for the Android home screen; keep it unlocked and in portrait.

Official [AVD instructions](https://developer.android.com/studio/run/managing-avds) describe the device/profile/image choices. If the integrated Running Devices view has rendering trouble, use the emulator's standalone window. Screenshot/mirroring limitations are covered below.

### Install the already-built APK

In a terminal:

```sh
cd /Users/abhijit/Developer/Codex
source .local/build-env.sh
adb devices -l
```

Find the emulator serial, such as `emulator-5554`. Replace the example with the actual listed serial:

```sh
test_device="emulator-5554"
adb -s "$test_device" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$test_device" shell am start -n dev.androidisland/.MainActivity
```

Expected: installation says `Success`, then Android Island opens in the emulator. `-s` selects the intended target when a phone and emulator are both connected.

### Run edited source from Android Studio

Use **Open** and select `/Users/abhijit/Developer/Codex`, then wait for Gradle sync. In **Settings → Build, Execution, Deployment → Build Tools → Gradle**, select JDK 17. The existing local JDK is `/Users/abhijit/Developer/Codex/.local/toolchains/jdk17/jdk-17.0.20.1+1/Contents/Home`. Select the `app` run configuration and virtual device, then press Run.

If Studio uses its own SDK, align the project's ignored `local.properties` with that SDK. Do not commit personal paths. The terminal build remains a useful independent check.

An emulator is suitable for layout, local tools, demo progress and many permission/lifecycle flows. It cannot prove real flashlight behavior, OEM background management or phone battery cost. Media controls also need an actual player/session in the virtual device; an empty emulator has none automatically.

## 4. Physical phone: USB installation from this Mac

### Prepare the phone

1. Use an Android 10+ phone and a USB cable that supports data.
2. In Android Settings, find **Build number** (usually About phone; Samsung may place it under Software information) and tap it seven times. Complete the device authentication prompt to enable Developer options.
3. Open **Developer options**, enable **USB debugging**, connect the phone to the Mac and unlock it.
4. Accept **Allow USB debugging?** on the phone for this trusted Mac. macOS does not need an additional OEM USB driver for standard adb setup.

See the official [physical-device guide](https://developer.android.com/studio/run/device). Leave permissions for this app denied initially so the reduced-mode tests are meaningful.

### Select, install and launch

```sh
cd /Users/abhijit/Developer/Codex
source .local/build-env.sh
adb devices -l
```

The phone should be listed with state `device`. Copy its actual serial into the variable; do not use the literal placeholder:

```sh
test_device="YOUR_PHONE_SERIAL"
adb -s "$test_device" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$test_device" shell am start -n dev.androidisland/.MainActivity
```

Expected: `Success`, then the app opens. Keep using that `test_device` value for later commands in this terminal. The [adb documentation](https://developer.android.com/tools/adb) covers installation, authorization and multiple-device targeting.

### Optional: Android 11+ wireless debugging

With the phone and Mac on the same trusted Wi-Fi, open Developer options → Wireless debugging → Pair device with pairing code. Use the pairing address/port shown by Android:

```sh
adb pair PHONE_IP:PAIRING_PORT
```

Enter the displayed pairing code when asked. Then use the separate connection address/port from Wireless debugging:

```sh
adb connect PHONE_IP:DEBUG_PORT
adb devices -l
```

Pairing and debugging ports can differ. Select the listed network serial as `test_device` and use the same install/start commands. Guest/corporate Wi-Fi can prevent discovery or connection; USB is the simpler first route. Never share the pairing code in a bug report.

## 5. Phone-only APK installation without adb

1. Transfer `app/build/outputs/apk/debug/app-debug.apk` to your phone using your preferred file-transfer method.
2. Open it in the phone's Files/Downloads app.
3. If Android asks, allow **Install unknown apps** for that specific file/browser app, then return and install Android Island.
4. Open Android Island from the app drawer.

This route does not require Developer options. Grant only access needed for each test. If Android blocks notification-listener settings for a sideloaded build, review its restricted-settings notice. For your own trusted build, some phones expose **Settings → Apps → Android Island → ⋮ → Allow restricted settings**. Follow [Google's restricted-settings guidance](https://support.google.com/android/answer/12623953); do not disable platform security globally.

## 6. Functional test checklist on either target

First launch with all optional permissions denied. Local search, math, emoji and explicit clipboard import should remain useful.

| Test | Steps | Expected outcome |
| --- | --- | --- |
| App launch | Apps → choose an installed app | Chosen app opens |
| Favorite | Apps → app's Edit → Favorite on → Close | It appears under All → Favorites |
| Alias | Edit an app → enter `yt, video` for a suitable app → Save; search `yt` under All | Correct app matches its alias |
| Visibility | Edit → Visible in search off; search under All | Hidden app is absent; Apps management can restore it |
| Persistence | Restart the app after saving preferences | Favorites, visibility and aliases survive; grid/list choice is currently session-only |
| Calculator | Search `2+3*4` | Numeric value 14; trailing zeros may be displayed |
| Invalid math | Search `1/0` | No infinite/invalid calculation result and no crash |
| Units | Search `10 km to mi` | Approximately 6.2137 mi |
| Temperature | Search `0 c to f` | Numeric value 32 F |
| Graph | Search `y=sin(x*2)*5,y2=floor(x)` | Two colored graph series with labels |
| Emoji | Emoji → search `hand` → select an emoji | Emoji copied; paste into a text field to verify |
| Clipboard import | Copy sample text in another app, return to Clips, tap Import | Item appears only after explicit import |
| Clipboard controls | Search, Copy, Delete; Pause then try Import; Resume; Clear all | Actions affect the bounded imported list as labeled |
| Agent demonstration | All → Run agent demo | Clearly labeled simulation progresses for about six seconds; it does not call AI |
| Cancel demo | Expand in-app island while demo runs → Cancel | Task becomes canceled |
| Unconfigured providers | Search a question or `10eth`, tap Ask AI | Accurate unconfigured message; no fabricated answer or live quote |

Use synthetic text such as `island-test-123`, not passwords or real private notifications.

## 7. Test the floating island carefully

### Enable and demonstrate

1. Keep the device unlocked and in portrait.
2. Expand the black in-app island card near the top.
3. Tap **Start floating island**. If Android opens display-over-other-apps settings, enable access for Android Island, return and tap **Start floating island again**. Granting permission alone does not start the session.
4. Optionally use app **Settings → Session notifications** to enable its ongoing notification on Android 13+.
5. Return to **All**, tap **Run agent demo**, and press the Android Home button immediately, while the demo is still running.
6. Look for a floating pill below the system status area. Tap it to expand; **Open commands** returns to the palette.
7. After the approximately six-second demo finishes, the floating window should disappear if music/torch activity is absent. The session can remain running but idle.

The overlay deliberately shows only active activity; a started session with no eligible activity has no floating window. Use music or flashlight for a longer observation window. Exact camera-hole/status-bar replacement is not promised. The pill uses generic summaries for privacy; media details are in the app.

### Stop, lock and rotate

- Stop via expanded floating pill → **Stop island**, in-app → **Stop floating island**, or the ongoing notification's **Stop** action. The window should disappear.
- While active, lock/screen-off: the window should disappear and imported clipboard content should clear.
- Unlock: a surviving session can resume an eligible activity; it should not replay old private clipboard contents.
- Landscape: this slice intentionally hides the floating overlay. Return to portrait to continue testing.
- Revoke display-over-other-apps access: the app should stop rendering and handle denial without a crash; after regranting, explicitly start again.

## 8. Phone tests: music and flashlight

### Music

1. App Settings → **Media access** opens Android notification-access settings.
2. Enable **Android Island media access** (label may be presented under Android Island), review Android's access explanation, then return.
3. Start playback in an installed player that publishes an Android media session.
4. Return to All: look for Now Playing, title/artist and only supported Play/Pause/Next actions.
5. Start the floating session if stopped, then return Home while playback continues. Expect the generic music summary.
6. Pause/play, change track, stop the session and try a second player. Record mismatches or missing supported actions.

A missing/paused/unpublished media session is different from a crash. Artwork and synchronized lyrics are not implemented yet. This listener integration currently uses media authorization and does not ingest notification message bodies.

### Flashlight

1. On a phone with a rear flash, tap **Flashlight** or expand the in-app card and tap **Torch on**.
2. Allow Camera permission when Android asks. The current public control path uses this permission.
3. Verify the actual LED and the app's callback state, then toggle it off again.
4. Test denial, camera contention (open Camera), and toggling the torch externally from Quick Settings.
5. With torch on and the island session active, go Home and check the generic flashlight activity.

No hardware flash or a camera in use may correctly show unavailable. An emulator cannot establish real LED behavior. Brightness control is not implemented. Closing the island is not an instruction to turn off a flashlight enabled elsewhere; test the actual system state rather than assuming exclusive ownership.

## 9. Restart, crash reporting and common issues

A controlled force-stop for your selected target:

```sh
adb -s "$test_device" shell am force-stop dev.androidisland
adb -s "$test_device" shell am start -n dev.androidisland/.MainActivity
```

Expected: preferences survive, transient clipboard/demo state does not survive process death, and the floating session needs an explicit restart. Force-stop and a normal Activity reopen are different lifecycle tests; record which you used.

| Symptom | Check |
| --- | --- |
| `adb: command not found` | Run `cd /Users/abhijit/Developer/Codex` and `source .local/build-env.sh` again |
| No device listed | Data-capable cable, unlocked phone, USB debugging and authorization; try another port/cable |
| Device says `unauthorized` | Accept the phone's debugging prompt for this Mac; reconnect if needed |
| Multiple devices | Use the actual serial with `adb -s` |
| Gradle rejects Java 26 | Source the local environment; confirm `java -version` reports 17 |
| SDK path error | Confirm `local.properties` points to the installed SDK |
| No floating pill | Portrait/unlocked, permission granted, session explicitly started, and music/torch/demo currently active |
| Music missing | Notification-listener access, connected state and a player publishing an active session |
| Import unavailable | Activity focused, imports not paused, actual text clip, not OS-marked sensitive content |
| Screenshot/recording/mirroring is black | Current app/overlay use FLAG_SECURE. This can block capture or mirroring even when the physical display works; do not treat it alone as a rendering failure |
| Install says update/signature incompatible | An existing same-ID app may have a different signing key. Removing it is an optional clean install and erases that app's local data |

For crashes, open Android Studio Logcat, choose the target device, and filter `package:dev.androidisland`. Record the exception/stack trace, build version, Android version/model, permission states, exact steps and expected/actual outcome. Scrub personal content; do not share whole-device logs or credentials. The FLAG_SECURE setting can prevent screenshots, so a written reproduction remains useful.

## 10. Scope and results record

Real AI execution/connectors, online AI answers, live crypto/fiat feeds, authorized lyrics, media artwork, app recents, persisted grid layout, priority/shortcut controls and notification/charging ingestion remain planned. REA is not needed to run any of these tests; its installation is still a separate pending approval.

Use [the experiment template](../templates/experiment.md) to record model, Android/API/build, APK version, test steps, result and limitations. Running this checklist on one emulator does not establish physical-device compatibility, battery performance or release readiness.
