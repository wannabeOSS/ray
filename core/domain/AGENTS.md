# Kotlin domain instructions

Follow root [AGENTS.md](../../AGENTS.md).

This module is pure Kotlin/JVM. Do not import Android, Compose or application classes. Arithmetic parsing must remain bounded and free from eval/reflection/I/O. Task transitions must reject stale revisions and terminal-state resurrection. Selection/privacy rules need deterministic behavior and an injected time input.

Test meaningful edge cases with `./gradlew :core:domain:test`: malformed/deep expressions, power precedence, graph gaps, unit dimension mismatches, hidden/alias ranking, stale task updates, expiry, bounded floods and privacy overrides. Source adapters and scheduling belong outside this module.
