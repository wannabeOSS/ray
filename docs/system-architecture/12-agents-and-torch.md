# 12 — AI agent activities and flashlight

Status: implementation kickoff design · Owner: Platform / Integrations

## AI agent activity model

An agent activity is a task being executed, not a coding-role document. Its contract contains an opaque task ID, connector ID, revision, safe user-approved label, state, optional progress, monotonic update time and advertised actions. States: queued → running → waiting-for-input/completed/failed/canceled. Waiting may return to running; terminal states cannot return to running without a new run ID. Completion percentage is optional; an indeterminate task must not invent progress.

Our in-process repository validates transitions and emits a bounded StateFlow. The first UI contains a clearly labeled simulation for design testing; it is not a connected AI model. Actual internal jobs can publish progress through the same contract. External agent integrations require authenticated transport, replay protection, per-connector sequence/epoch, ownership checks and explicit task authorization. No exported broadcast receiver accepts arbitrary progress or executable commands in the scaffold.

Starting or canceling a task requires an explicit action. The island observes progress; it does not autonomously start agents. A future executor chooses appropriate foreground/background scheduling for each task instead of assuming the overlay service permits arbitrary long-running work. Process death currently ends demo tasks; durable job recovery is planned separately.

Island priority starts with an explicitly expanded activity, then a new transient, active task, torch-on and media, using stable tie-breaking. Privacy guards precede priority. Task content is generic by default and hidden at lock. Multiple tasks appear as a count and bounded list in expanded presentation.

## Flashlight lifecycle

CameraManager enumerates a flash-capable rear camera. Register TorchCallback while an active app/overlay session needs observation, unregister when unused, and treat callback state as authoritative. Separate off, on and unavailable; camera contention and thermal/system restrictions can make it unavailable. Contextual CAMERA permission is requested only when the user chooses flashlight control. Denial keeps the rest of the product usable.

Only explicit user actions toggle the torch. Do not toggle automatically on island attach, preview or process restart. Stop/lock behavior must distinguish a torch this app enabled from one enabled by another app; never turn off an externally owned torch just because a UI closes. A callback cannot prove exclusive ownership. Hardware output may persist independently of window visibility and must be tested physically.

The initial adapter implements on/off; brightness is documented as a future capability-gated control using API-level and hardware strength metadata, not a universal slider. Catch camera/security/argument failures and show a recoverable unavailable state. No camera preview, image capture or microphone access is part of this feature.

## Validation

Test no flash hardware, permission denied/revoked, camera opened by another app, external torch toggles, process death, lock/unlock and thermal unavailability. Test agent stale revisions, invalid transitions, multiple tasks, cancellation, terminal state retention, disconnect and reconnect. An APK build establishes compilation only; these checks need runtime/device evidence.

Primary references: [CameraManager](https://developer.android.com/reference/android/hardware/camera2/CameraManager), [Android clipboard privacy](https://developer.android.com/about/versions/10/privacy/changes#clipboard-data), and [shortcut management](https://developer.android.com/develop/ui/compose/system/shortcuts/managing-shortcuts).
