# ADR-001 — Native Android application

Status: accepted · Date: 2026-10-17 · Owner: Architecture

## Context

The product is dominated by Android notifications, media sessions, window management, and service lifecycle. Fast animations alone do not resolve permission and background constraints.

## Decision

Use Kotlin with coroutines/Flow and Compose for the app and island presentation, with a small native View overlay for the initial feasibility slice; benchmark a Compose overlay separately. Keep the event reducer pure Kotlin and Android dependencies inside adapters. Use a modular monolith in one process.

## Alternatives

Flutter or React Native could share presentation work across platforms but would still require substantial Android integration and bridge lifecycle coordination. A WebView surface increases rendering and integration complexity without a current web product requirement. A custom Android View renderer remains a fallback if profiling proves Compose cannot meet the agreed budget.

## Consequences

Android-specific expertise and device testing are required. Domain tests remain fast and isolated. Compose-in-service lifecycle ownership must be explicitly implemented and tested; an Activity preview alone is insufficient evidence.

## Acceptance evidence

PLAN-001 proves rendering and cleanup; PLAN-003 proves accessible interaction; PLAN-008 measures budgets. The user explicitly selected Kotlin and authorized implementation on 2026-10-17. Device/performance validation remains pending.
