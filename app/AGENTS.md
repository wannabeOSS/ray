# Android implementation instructions

Follow root [AGENTS.md](../AGENTS.md), [status](../docs/implementation/status.md) and [development guide](../docs/implementation/development.md).

Keep Compose UI on the main thread; run app indexing and graph sampling off it. Platform adapters own and release callbacks explicitly. No platform objects in the pure domain module. No network permission or provider credentials until a real documented integration is wired.

Permission prompts must follow explicit user actions. Recheck lock and source capabilities at dispatch. Never read the clipboard in a background service or ingest real notification bodies for debugging. No exported task-progress receiver without a defined authentication contract.

Use `./gradlew :core:domain:test :app:assembleDebug :app:lintDebug`. Treat physical-device compatibility, overlay focus, camera hardware and battery performance as unverified until evidence exists. The native View overlay is an intentional initial lifecycle spike; Compose owns the focused Activity.
