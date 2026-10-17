# Kotlin kickoff validation

Tasks: PLAN-011, PLAN-012 · Date: 2026-10-17

## Observed results

| Check | Result |
| --- | --- |
| Full reference inventory | All nine screenshot panels mapped, including app/command tools and agent/torch additions |
| Gradle wrapper/distribution | Publisher SHA-256 verified; distribution hash pinned |
| Kotlin domain tests | 12 passed, 0 failures, 0 errors, 0 skipped |
| `:app:assembleDebug` | Passed; debug APK produced |
| `:app:lintDebug` | Passed; 0 errors; 11 dependency/toolchain update recommendations |
| APK permissions | Overlay, foreground service/special-use, notifications and camera; no INTERNET, microphone, usage-access or broad package query permission |
| Documentation checks | Passed local file links, task dependencies/criteria, ADR references and all 32 requirement IDs |
| Device/emulator smoke tests | Not run; no device compatibility, focus, hardware or performance claim |

Command used after sourcing the project-local build environment:

```sh
JAVA_TOOL_OPTIONS='-Djava.net.preferIPv4Stack=true' ./gradlew :core:domain:test :app:assembleDebug :app:lintDebug --no-daemon --console=plain
make docs-check
```

The clean first build completed successfully. After resolving three source-level lint suggestions, the final incremental build completed successfully in 20 seconds. Remaining lint recommendations concern newer dependency/toolchain versions; the deliberate compatible baseline is documented in the development guide. The SDK metadata parser and optional native-symbol stripping produced informational build warnings; the APK was assembled successfully. Recheck tooling/native release requirements before production.

## Artifact

- Local debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Size: 32798426 bytes
- SHA-256: `799383d32ada0ccafb0b77fa8d8937cf64dcb7f97d606d98cd0c672b44f4b3cf`
- Application ID: `dev.androidisland`; version `0.1.0-dev`; minimum API 29, target API 36
- HTML lint report: `app/build/reports/lint-results-debug.html`
- JVM results: `core/domain/build/test-results/test/TEST-dev.androidisland.domain.DomainTest.xml`

Build artifacts and local toolchains are ignored, not committed deliverables. The source, wrapper and pinned catalog reproduce the build with an appropriate SDK/JDK environment. No release-signing or store upload was performed.

## Scope limits

A build validates compilation and static checks, not real device behavior. The UI's agent activity is a labeled simulation. Real AI execution/connectors, online answers, rate feeds, authorized lyrics, priority/shortcut/recents polish and notification/charging ingestion remain staged tasks. The first production event actor is not fully wired; the initial overlay uses privacy-guarded current platform state. M0, M3 and public-release gates remain open. REA setup remains pending its separate requested approval.
