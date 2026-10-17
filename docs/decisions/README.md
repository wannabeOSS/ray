# Architecture decision register

Decision status is explicit per record; Kotlin and the expanded two-surface direction are accepted for kickoff. Acceptance requires an owner, evidence, and date; do not silently promote status when implementation begins.

| ID | Decision | Status | Revisit trigger |
| --- | --- | --- | --- |
| [ADR-001](ADR-001-native-android.md) | Native Kotlin with separated domain engine | Accepted | Overlay renderer benchmark or lifecycle spike fails |
| [ADR-002](ADR-002-overlay-lifecycle.md) | Public overlay with explicit user-started lifecycle | Proposed | FGS eligibility, policy or device reliability gate fails |
| [ADR-003](ADR-003-local-data.md) | Local-only processing and ephemeral content | Superseded by ADR-005 for online scope | New feature needs storage/networking |
| [ADR-004](ADR-004-event-engine.md) | Serialized deterministic reducer | Proposed | Measured throughput cannot meet bounded latency |

| [ADR-005](ADR-005-command-palette-and-providers.md) | Island + command palette and optional providers | Accepted | Provider consent/data flow changes |

Use the [ADR template](../templates/adr.md). Number new records sequentially. Supersede an accepted record with a new record rather than rewriting its original rationale. Tool installation status belongs in [tooling](../tooling/rea-verification.md), not an application architecture ADR.
