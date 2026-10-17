# ADR-005 — Island plus command palette and optional providers

Status: accepted · Date: 2026-10-17 · Owner: Architecture

## Context and authorization

The user confirmed that agent activities mean AI agents executing tasks, requested flashlight activity, selected Kotlin, authorized implementation kickoff and provided a nine-panel launcher reference. The earlier island-only scope does not cover the requested product.

## Decision

Build two connected surfaces in one native Kotlin app: an island for live activities and a focused command palette for search/actions. Include every observed reference feature in the feature map and staged backlog. Start with local functions and platform adapters. Define AI, currency/crypto and lyrics as optional online provider contracts; provider selection, credentials and transport remain unresolved and no online request is made in the first slice.

This supersedes ADR-003's blanket no-network product scope for the expanded product. Its prohibition on notification-content upload, generic preview defaults and memory-only transient data remain. User-selected AI prompts may be sent only through a configured provider with explicit Send. Clipboard remains foreground/manual and memory-only until a separately reviewed retention feature exists.

## Alternatives and consequences

A separate launcher app duplicates preferences and activity state. An overlay text editor complicates focus/IME and clipboard access. A normal Activity provides a dependable first entry point and leaves home-launcher replacement optional. Network features add consent, credential and service operations work; they are not implied to be working by the presence of result cards.

## Evidence and revisit

Authorization is the user's 2026-10-17 project expansion message. The [reference map](../product/raycast-feature-map.md) records observations, adaptations and acceptance. Performance, physical-device compatibility and release approval remain unproven. Revisit if the focused Activity cannot satisfy invocation needs or a provider requires an unacceptable data flow.
