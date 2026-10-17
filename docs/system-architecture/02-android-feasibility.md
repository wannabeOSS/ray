# 02 — Android feasibility and permission model

Status: proposed with documented platform constraints · Owner: Android Platform

## Capability matrix

| Capability | Public mechanism | Limit and fallback | Gate |
| --- | --- | --- | --- |
| Draw overlay | `SYSTEM_ALERT_WINDOW`, `Settings.canDrawOverlays`, `TYPE_APPLICATION_OVERLAY` | Below critical system UI; OS may relocate/hide it. Fall back to in-app preview | PLAN-001 |
| Read notifications | User-enabled `NotificationListenerService` | Some content/profiles unavailable; treat missing data as unavailable | PLAN-002 |
| Control media | `MediaSessionManager` and source `MediaController` | Enabled listener can access eligible active sessions; show only advertised supported controls | PLAN-002 |
| Charging signal | Registered battery/power broadcasts while active | Changes only; no permanent polling or background wake-up guarantee | PLAN-006 |
| Keep user-requested overlay session | Foreground service with suitable declared type | `specialUse` is a candidate requiring justification/review; explicit start from visible Activity | PLAN-001 |
| Post service/app notifications | Notification channel; `POST_NOTIFICATIONS` where required | Denial differs from listener access and does not itself prohibit FGS; app still must supply its FGS notification | PLAN-001 |
| Cutout-aware placement | Display metrics, insets, cutout bounds | Overlay-window geometry needs its own validation; calibration and below-status-bar fallback | PLAN-001 |

The overlay layer is not trusted system UI and cannot promise exact status-bar or camera-hole behavior. See [Android window reference](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY).

## Proposed declarations

These are a review checklist, not a committed manifest:

- `SYSTEM_ALERT_WINDOW` special access, requested only when enabling the external overlay.
- `FOREGROUND_SERVICE` and, if the use case qualifies, `FOREGROUND_SERVICE_SPECIAL_USE` plus an honest `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` description.
- `POST_NOTIFICATIONS` on applicable Android versions for this app's notifications.
- Listener service protected with `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`, the documented service intent filter, and explicit export configuration required for system binding. This is a service binding permission, not a user runtime permission request.
- Nonexported overlay host; minimal exported launcher entry point. Audit every additional component.

Do not request `MEDIA_CONTENT_CONTROL` as a normal app permission; the proposed media path uses enabled notification-listener access. No `READ_PHONE_STATE`, contacts, microphone, location, usage stats, accessibility service, `QUERY_ALL_PACKAGES`, exact alarms, or battery-optimization exemption in MVP. No `INTERNET` permission unless an approved later feature needs networking.

## Background execution gate

Start the service from an explicit Start action while the Activity is visible; enter foreground promptly and provide Stop. Background notification arrival must not silently start it. On target Android 15+, the overlay-permission exception for background FGS starts additionally requires an already visible overlay; it cannot bootstrap a stopped overlay. See [FGS start restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).

The proposed `specialUse` type must describe a real use case and is subject to Google Play review. Do not use `mediaPlayback` merely because the app displays another app's media. If no appropriate type is defensible, stop this implementation route and evaluate a reduced in-app/notification experience. See [service types](https://developer.android.com/develop/background-work/services/fgs/service-types#special-use).

## API and device support

Proposed minimum API 29 balances coverage and scope. Select compile/target SDK and exact dependencies at kickoff from current stable guidance and distribution requirements; do not freeze an outdated target in this plan. Test API 29, 31, 33, 34, 35, and the latest stable API chosen at kickoff, deduplicating overlaps.

Platform checks include Android 12 overlay touch restrictions, Android 13 notification permission denial, Android 14 foreground type requirements, Android 15 sensitive-content redaction and FGS behavior, and later changes discovered at kickoff. Low-RAM Android 10 devices and managed profiles can limit listeners; publish only verified support. [Listener reference](https://developer.android.com/reference/android/service/notification/NotificationListenerService) and [Android 15 changes](https://developer.android.com/about/versions/15/behavior-changes-all).

Pilot matrix proposal: Pixel/AOSP, Samsung One UI, and one aggressive background-management OEM selected with the user. Use physical phones for cutouts, battery, TalkBack overlay focus, and process management. Emulators alone cannot close the gate.

## Required experiment result

Record grant/deny/revoke flows, overlay touch bounds, cutout/rotation screenshots, user-started service behavior, lock/screen-off cleanup, listener reconnect, force-stop, and OEM termination. Mark each capability supported, degraded, or unsupported per device. A feature is not approved just because its API exists.
