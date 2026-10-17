# ADR-004 — Deterministic serialized state engine

Status: proposed · Date: 2026-10-17 · Owner: Domain Engine

## Context

Notification updates, source removals, media changes, user gestures, and privacy changes can race. UI-owned mutable lists would make ordering and stale actions difficult to reason about.

## Decision

Normalize sources into typed events and reduce through one serialized actor with an injected monotonic clock. Use bounded/coalesced data queues, non-droppable control invalidation, source epochs and immutable render state. The engine contains no Android platform objects.

## Alternatives

Independent feature flows directly updating UI reduce initial wiring but spread arbitration across components. Persisting all events in a database adds retention and ordering overhead the product does not need. Multiple engine actors add coordination complexity without measured demand.

## Consequences

Replayable tests and explicit backpressure become straightforward. The actor must stay nonblocking; decoding and platform calls run outside it. Control-path priority and snapshot races require adversarial tests.

## Acceptance evidence

PLAN-005 validates invariants and queue semantics; PLAN-008 verifies latency under stress. A measured throughput problem can trigger re-evaluation.
