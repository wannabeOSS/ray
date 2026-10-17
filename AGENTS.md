# Agent operating instructions

## Scope and phase

This is the Kotlin implementation and planning repository for Android Island plus its command palette. The user authorized implementation kickoff on 2026-10-17 and confirmed AI-agent task progress, flashlight activity and the attached launcher feature reference. Maintain architecture, requirements, source code, research evidence and delivery plans together. Do not present proposed APIs, performance budgets, or device support as verified results.

Read [README.md](README.md), [the architecture index](docs/system-architecture/README.md), [the agent handbook](docs/agents/README.md), and the relevant task before editing. Nested AGENTS.md files add instructions for their directory.

## Product invariants

- Use public Android APIs and respect revoked permissions, system suppression, force-stop, and user pause.
- Keep notification text, media artwork, action tokens, and source identifiers out of persistent logs, test artifacts, and network requests.
- Default to hidden on the lock screen and when the screen is off. Never infer that overlay permission grants status-bar control or unrestricted background execution.
- Do not add accessibility-service, root, hidden-API, usage-access or broad package visibility to bypass platform constraints. Online AI/rates/lyrics are requested features; implement only documented explicit-send provider flows. Never upload notification content implicitly.
- Keep REA and its evidence separate from shipped application code. Analyze only user-authorized targets; no third-party target is authorized by installing REA.

## Work protocol

1. State the task ID or create a scoped planning task with acceptance criteria.
2. Read the relevant ADRs and platform references. Record assumptions and evidence gaps.
3. Change the smallest coherent set of documents. Keep cross-references and backlog status consistent.
4. For a changed architectural decision, add or supersede an ADR; preserve its history.
5. Run `make docs-check`. For later application work, run the checks defined by that module and task.
6. Report changed files, validation performed, remaining risks, and a concrete handoff.

Role documents describe responsibilities, not automatically running agents. Delegate only when the user or applicable higher-priority instructions authorize it. Shared files need one active editor at a time; agree on file ownership before parallel work.

## Tools and approval

The user explicitly requested review of the REA setup plan before installation. Do not run setup with apply/yes flags, modify personal MCP configuration, or install the REA skill until that approval is recorded. Read-only metadata, package inspection, and `setup --dry-run` are allowed for preparing the plan. See [REA setup](docs/tooling/rea-setup-plan.md).

Do not replace unrelated user configuration. Do not commit local tool evidence, secrets, binaries, credentials, or signing keys. Reversible documentation and implementation work within this workspace is authorized. REA approval is separate and still pending.

## Implementation workflow

Read [feature map](docs/product/raycast-feature-map.md), [current status](docs/implementation/status.md), and [development guide](docs/implementation/development.md). Use Kotlin. Pure computation and task/event rules belong in `core/domain`; platform objects stay in `app`. The first slice deliberately precedes completion of physical-device feasibility work; never mark M0 or public-release readiness complete from an APK build.

Run `./gradlew :core:domain:test :app:assembleDebug :app:lintDebug` for implementation changes, using JDK 17/SDK 36. Run `make docs-check` for documentation changes. Keep `.local`, `local.properties`, build outputs and keys out of source control. Do not label simulated agent progress or unconfigured online cards as working integrations.
