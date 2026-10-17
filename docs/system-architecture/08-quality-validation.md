# 08 — Quality and validation strategy

Status: proposed · Owner: QA / Performance

## Verification layers

| Layer | Scope | Tooling proposal | Exit criterion |
| --- | --- | --- | --- |
| Documentation | Links, required files, backlog structure | Python standard-library checker | `make docs-check` passes |
| Domain unit/property tests | Reducer, ordering, bounds, TTL, arbitration | Kotlin unit tests, virtual clock, property framework selected at kickoff | All invariants and adversarial sequences pass |
| Adapter integration | Listener snapshots, media capabilities, action expiry | Android instrumented tests + synthetic source app | Version-aware contract fixtures pass |
| Renderer and interaction | Geometry, focus, large text, RTL, animation | Compose tests plus physical-device validation | FR-07/FR-10 evidence |
| Lifecycle | Kill, lock, revoke, disconnect, rotate | Device scripts and manual OS settings flows | No leaked data, window, or restart |
| Performance | Callback latency, frames, memory, idle battery | Trace sections, Perfetto, Macrobenchmark, paired battery runs | NFR budgets or explicit accepted revision |
| Release/privacy | Manifest, backup, network, storage, dependencies | Artifact inspection and synthetic markers | No unexpected permissions, content egress or persistence |

## Canonical scenarios

1. Post → update → expand → remove while user taps; removal invalidates the action.
2. Delayed update from old source epoch arrives after reconnect; it is discarded.
3. Revoke access while expanded; next frame contains no content and the window detaches.
4. Receive 100 notifications/second for 60 seconds; bounded queues, prompt Stop, preserved removals, no OOM/ANR. This is a stress proposal, not typical load.
5. Two media sessions alternate; controls always address the selected valid session.
6. Rotate, resize, and fold while expanded; window remains within safe bounds or hides.
7. Pause/stop while decoding artwork; job cancels and no old image appears after restart.
8. Lock between action validation and dispatch; final guard rejects dispatch if invalidated.
9. Set clock backwards; TTL behavior is unchanged because it uses elapsed time.
10. Deny all permissions; demo and settings remain useful and accurate.
11. Kill process and separately force-stop; neither causes a hidden automatic restart.
12. Change source allowlist during an event flood; excluded items and queued actions clear.

## Device matrix

Use the API bands in [feasibility](02-android-feasibility.md) and at least the three pilot OEM categories. Include central and off-center punch holes, no cutout, 60/90/120 Hz where available, light/dark, portrait/landscape, large font, RTL, TalkBack, gesture/three-button navigation, battery saver, notification denied, DND/quiet signals, and low-memory conditions. Record unsupported cells explicitly; do not claim coverage from an unrun matrix.

## Performance protocol

For latency, trace callback receipt, normalization end, reducer commit, render submission, and first presented frame. Measure ≥100 events after warm-up on each pilot model. Report p50/p95/p99, frame refresh rate, build type and thermal state. Separate startup latency from steady-state latency.

For battery, run paired baseline/enabled scenarios with identical screen/network/activity conditions, repeated ≥3 times. Report percentage-point delta, duration, OS battery attribution, temperature and uncertainty. Do not extrapolate a five-minute trace into an eight-hour claim.

Memory target starts at incremental steady-state PSS ≤50 MiB above a stopped-app baseline and no growth trend across 100 start/stop cycles. Validate the 4 MiB artwork cap and queue bounds separately; profile native/graphics allocations as well as JVM heap. Budget changes require measured rationale.

## Evidence record

Every result names requirement/task IDs, device model, OS build/API, target SDK, app commit/build type, setup, exact steps, expected/actual result, artifact path and reviewer. Use [experiment template](../templates/experiment.md). Keep raw personal device logs out of the repository; commit only scrubbed synthetic evidence.

Planning checks cannot validate Mermaid rendering, app behavior, policy acceptance, or performance. Those remain future milestone evidence.

## Expanded command-palette and activity coverage

FR-11–FR-23 add task progress, torch control and the full [reference feature inventory](../product/raycast-feature-map.md). Add scenarios for alias/visibility persistence, uninstall during app launch, local search latency at 500 apps, safe-parser hostile input and recursion bounds, graph discontinuities, unit dimensions, Unicode emoji copy, sensitive clipboard rejection, foreground focus loss and lock clearing.

Real-agent tests must cover authenticated ownership, duplicate/stale revisions, waiting/resume, terminal states, canceled requests, new-run retry IDs and disconnect/reconnect. Provider tests need explicit Send, model identity, generation cancellation, secret handling, offline/stale rates and absence of invented history. Lyrics tests need authorized data, session alignment, user offset and unavailable states. Torch tests require real hardware, camera contention, denied/revoked access and external toggles.

The initial JVM suite tests parser/ranking/task/engine rules. It does not replace device integration tests for adapters or a real online provider test. [Current build evidence](../implementation/status.md) remains the source of truth for what has actually run.
