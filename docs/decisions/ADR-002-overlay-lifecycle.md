# ADR-002 — Public overlay with explicit lifecycle

Status: proposed · Date: 2026-10-17 · Owner: Android Platform

## Context

Cross-app display requires special access and remains subordinate to Android UI and background execution policy. An always-running promise cannot be made across devices.

## Decision

Use `TYPE_APPLICATION_OVERLAY`, explicit permission education, and a user-started host session. Evaluate a correctly declared `specialUse` foreground service. Provide Pause/Stop, hide at lock/screen-off, and require explicit restart after process death. No accessibility-service workaround or automatic boot startup in MVP.

## Alternatives

In-app preview avoids overlay permissions but does not satisfy cross-app use. Standard notifications or supported system surfaces may form a reduced fallback. Privileged/system integration would need a different distribution and partnership model; it is outside scope.

## Consequences

Capability denial yields a useful reduced mode. Exact cutout/status-bar placement is not guaranteed. OEM behavior and store review can block this route. Declaring `specialUse` is not approval.

## Acceptance evidence

PLAN-001 and PLAN-004 must document technical behavior and a defensible distribution plan. If they fail, product scope is revisited before the app scaffold.
