# 07 — Security and privacy

Status: proposed · Owner: Security / Privacy

## Assets and threats

| Asset / boundary | Threat | Control | Validation |
| --- | --- | --- | --- |
| Notification content | Private messages or OTPs exposed | Generic previews by default; memory only; hide at lock; respect OS redaction | Synthetic secret markers absent from disk/log/export |
| Overlay touch surface | Obscured controls or misleading system UI | Small bounded window, distinct app identity, no fullscreen transparent layer | Tap outside bounds reaches intended app on supported API/device |
| Source actions | Stale, duplicate, or unintended action | Current-revision handle; explicit tap; permission and lock recheck | Race and double-tap tests |
| Content payload | Excessive text, malformed spans, malicious image | Normalize plain text, enforce budgets, bounded decode | Fuzzed synthetic payloads |
| Local preferences | Backup leakage or corruption | Backup exclusion, validation and migration | Backup config and corruption tests |
| Exported components | External app starts service or injects commands | Minimal exports, platform binding protection, no custom public IPC | Manifest and intent audit |
| Developer tooling | REA target/evidence leaks into shipped app | Separate tooling and ignored local evidence | Build dependency and artifact inspection |
| Dependencies | Compromised artifact or drifting versions | Pin versions, review changes and checksums, no dynamic build versions | Dependency review at scaffold and release |

Android overlays are untrusted windows and touch delivery is constrained. Do not bypass obscured-touch protections or applications that hide overlays. [Tapjacking guidance](https://developer.android.com/privacy-and-security/risks/tapjacking) informs this constraint; the bounded-window design is our proposed mitigation, not a guarantee of approval.

## Privacy lifecycle

Permission education must explain that notification access can expose broad content before the app's own filtering. Offer source selection and preview controls immediately. Revoking access clears cached content and actions. Stop clears the full transient store. No content survives lock/unlock without a fresh allowed snapshot.

OS-redacted notifications remain redacted. Classification is best effort; the app cannot promise to recognize all sensitive text. Default generic previews are the reliable privacy baseline. Work-profile content is unsupported unless separately evidenced and permitted by device policy.

## Diagnostics and exports

Allow codes such as `overlay_attach_failed`, `listener_disconnected`, `event_dropped`, and numeric timing/queue counts. Exclude titles, bodies, package names, notification keys, URIs, media metadata, raw exceptions that could contain payloads, and action objects. A debug build does not relax this rule for real user data.

No automatic crash-upload SDK in MVP. A future analytics/crash feature requires a data inventory, consent decision, transport design, retention policy, and ADR. Developer tools may use networking; that does not imply the product uses networking.

## Review and incident handling

Security review covers manifest changes, action dispatch, permission onboarding, storage, backup, dependencies, and release artifacts. A privacy regression blocks release. For a discovered leak: stop reproduction using real data, use synthetic fixtures, document affected versions, disable the affected local feature in a patch, and prepare factual user-facing guidance. There is no remote kill switch in the offline MVP.

See [SECURITY.md](../../SECURITY.md) for contributor reporting and [delivery](09-delivery-and-operations.md) for release controls.
