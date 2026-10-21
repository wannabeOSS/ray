# 09 — Delivery, release and operations

Status: proposed · Owner: Delivery / Tooling

## Environments

| Environment | Purpose | Data |
| --- | --- | --- |
| Planning workspace | Requirements, ADRs, task graph, source notes | No app data |
| Developer build | Synthetic adapters, traces and device experiments | Synthetic notifications only |
| Internal pilot | Permission, OEM and battery validation | Volunteer-controlled local data; manual redacted reports |
| Release | Reviewed app bundle and store listing | Local data plus explicit optional provider requests under ADR-005 |

## Repository and build strategy

The current folder contains documentation and the initial Kotlin app in a local Git repository. On 2026-10-21 the user selected [wannabeOSS/ray](https://github.com/wannabeOSS/ray) as the publication destination. PLAN-019 tracks the push and remote verification. Issue/PR templates and workflow files activate when hosted; branch protection and CI results require separate observed evidence.

The kickoff creates `app` and `core/domain` with pinned Gradle/AGP/Kotlin/JDK/SDK versions and a checksum-verified wrapper. Continue splitting modules according to [module boundaries](03-components-and-modules.md) as integrations grow. Maintain the version catalog and review dependency verification before release; update toolchain pins as a compatible set using official guidance. The exact kickoff baseline and observed build results live in the [development guide](../implementation/development.md).

Keep signing material and `local.properties` outside source control. Select license and namespace/application ID with the product owner before public distribution. Keep SDK paths and developer-specific tooling out of product builds.

## CI progression

Current prepared job: docs validation with read-only repository permissions, no secrets, and a finite timeout. It is not an Android build pipeline.

Future PR jobs: documentation checks → formatting/static analysis → domain tests → Android build/lint → selected emulator integration tests. Release candidate jobs additionally run supported-device suites, benchmark collection, dependency/license review, manifest and backup audit, signed bundle generation via protected secrets, and manual release approval. Pin CI actions to verified commit SHAs during repository activation.

## Release gates

1. M0 feasibility evidence and architecture decisions reviewed.
2. MVP requirement traceability complete and no unresolved high-severity privacy/lifecycle defect.
3. Device compatibility list matches tested builds; known limitations documented.
4. Foreground service, notification access disclosures, and distribution requirements rechecked at submission.
5. Privacy policy and store disclosures match actual behavior; do not claim no data access merely because processing is local.
6. Signing, versionCode, upgrade migration, rollback compatibility and dependency licenses reviewed.
7. Pilot acceptance and staged rollout decision recorded by the owner.

Google Play approval is an external gate, not a result inferred from this architecture. Other distribution channels also require a deliberate product decision.

## Rollback and support

Pause or stop the local feature immediately from the app or ongoing notification. For a release regression, halt rollout and publish a corrected build; Android generally does not allow a normal lower-version downgrade, so rollback often means a new versionCode containing reverted code plus compatible migrations. Retain the previous release source and signed artifact under the chosen release policy.

Support diagnostics show capability state, app version, source connection health and counts. Do not show notification contents. Users explicitly review any exported report. Maintain a device compatibility document as experiments produce evidence; do not advise unrestricted battery access as a default fix.
