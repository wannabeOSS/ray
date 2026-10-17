# 05 — Contracts and data ownership

Status: proposed · Owner: Domain Engine / Privacy

## Internal contracts

The contracts below are conceptual Kotlin shapes, not compiled source or a public API. `EventKey`, `SourceEpoch`, `Revision`, and `ActionHandle` are opaque value types. Domain modules do not depend on Android classes.

```kotlin
data class IslandEvent(
    val key: EventKey,
    val epoch: SourceEpoch,
    val revision: Long,
    val kind: EventKind,
    val receivedAtElapsedMs: Long,
    val expiresAtElapsedMs: Long?,
    val safeContent: SafeContent,
    val actions: List<ActionHandle>
)

sealed interface EngineInput {
    data class Upsert(val event: IslandEvent) : EngineInput
    data class Remove(val key: EventKey, val epoch: SourceEpoch, val revision: Long) : EngineInput
    data class CapabilitiesChanged(val snapshot: CapabilitySnapshot) : EngineInput
    data class UserIntent(val intent: IslandIntent) : EngineInput
    data class DeadlineReached(val elapsedMs: Long) : EngineInput
}
```

| Contract | Producer → consumer | Guarantees |
| --- | --- | --- |
| `SourceSnapshot(epoch, items)` | Adapter → engine | Replaces that source generation atomically after reconnect; no replay of expired transient alerts |
| `CapabilitySnapshot` | Platform → engine/dispatcher | Observed values plus generation; Unknown is restrictive, never implicitly granted |
| `IslandRenderState` | Engine/presenter → renderer | Immutable, already redacted, bounded, contains no platform action objects |
| `ActionRequest(key, revision, handle, generation)` | UI → dispatcher | Must match current event and capabilities immediately before dispatch |
| `ActionResult` | Dispatcher → engine | Success means dispatch accepted, not that the third-party operation completed |
| `DiagnosticEvent` | Components → local diagnostics | Enum code, monotonic timing and numeric measurements only |

`SafeContent` contains a source label, category, and optional user-approved preview fields. Length limits proposed: 80 characters for label, 160 for title, 320 for body, four source actions. Trim at Unicode boundaries; remove control characters; never interpret markup, HTML, file paths, or commands. Allow meaningful multiline text only in expanded view. Empty/malformed payloads show a generic category or are discarded.

## Action semantics

An ephemeral Android-side registry maps action handles to current `PendingIntent` or media-controller operations. Handles are never serialized. Only explicit taps execute actions. Recheck unlocked state, session enablement, permission, source epoch, current revision, and advertised media capability. Reject double taps while one dispatch is pending. Cancel an action if the event changed under the user.

Use source-provided OS-mediated actions; never rebuild intents from notification text or package-controlled URLs. Catch canceled intents and security failures. Respect applicable background activity launch restrictions and avoid broad launch opt-ins; if a source action cannot be launched, show a safe explanation in the app. Direct reply and arbitrary text input are outside MVP.

## Storage and retention

| Data | Location | Retention / deletion |
| --- | --- | --- |
| Theme, position, motion, privacy flags | App-private DataStore | Until reset or uninstall; schema-versioned |
| Source allowlist | DataStore | Until removed/reset/uninstall; treat as sensitive app-usage preference |
| Notification metadata and text | RAM only | Until removal, expiry, lock, source invalidation or stop |
| Media metadata and artwork | Bounded RAM cache | Until session invalidation, lock, stop or eviction |
| PendingIntent/session handles | RAM only | Until source revision invalidates them or lifecycle cleanup |
| Diagnostic counters | RAM ring buffer | Session only; explicit redacted export |
| Build/version and compatibility reports | Developer repository, synthetic data only | Reviewed retention per release |

Proposed artwork cap: decode to at most 256×256 pixels per item, aggregate 4 MiB cache, decode timeout and cancellation. Treat URIs and image decoders as untrusted input; use permitted content access without fetching remote artwork URLs. Cache ownership stays outside the domain engine.

Preferences schema v1: `schemaVersion`, `previewMode`, `theme`, `motionMode`, bounded placement/size values, `allowedSources`, and `landscapeMode`. Do not persist runtime permission grants or assume a persisted enabled flag authorizes service restart. Runtime desired state lives within the active session; after process restart show Stopped.

## Migration and backup

Every preference change needs an explicit migration test and safe default. Validate corruption or out-of-range placement before rendering; offer Reset appearance without clearing unrelated preferences. A downgrade encountering a newer schema must fail safely and avoid destructive rewrites until supported migration or user reset.

Exclude preferences and diagnostics from Android cloud backup/device transfer for MVP, or explicitly review an allowlist before enabling backup. Never persist transient content in saved-instance state, crash reports, screenshots, or automatic test recordings. User-requested diagnostic export shows exactly what will be shared.
