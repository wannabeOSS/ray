# ADR-003 — Local-only and ephemeral event data

Status: superseded · Date: 2026-10-17 · Owner: Privacy

Superseded in online-provider scope by [ADR-005](ADR-005-command-palette-and-providers.md). Memory-only notification handling and explicit privacy controls remain design requirements.

## Context

Notification and media content can be highly personal. The MVP needs immediate display rather than history, synchronization or analysis.

## Decision

Process events in memory, persist only validated preferences, omit networking and accounts, and keep diagnostics content-free. Generic previews are the default. Clear event content on lock, stop, invalidation and expiry. Exclude app data from cloud backup/transfer unless a later review defines a narrow safe allowlist.

## Alternatives

A persistent notification database enables history but materially expands retention and deletion obligations. A backend permits analytics/sync but adds data transfer, authentication and operational requirements absent from the MVP.

## Consequences

No historical recovery after process death. Debugging uses synthetic events and explicit redacted reports. Preference allowlists still reveal user interests and require care despite containing no message text.

## Acceptance evidence

PLAN-004 reviews disclosures and storage boundaries. PLAN-007 and PLAN-008 validate lifecycle clearing, storage, logs, and network behavior with synthetic markers.
