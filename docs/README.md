# Documentation map

All designs are proposed unless an ADR explicitly records acceptance and supporting evidence. Owners below are project roles, not assigned people.

| Area | Entry point | Accountable role |
| --- | --- | --- |
| Product | [Requirements and acceptance criteria](product/requirements.md) | Product |
| Screenshot inventory | [Every requested reference feature](product/raycast-feature-map.md) | Product |
| Laptop and phone testing | [Install, run, test and troubleshoot](implementation/testing.md) | Delivery / QA |
| Kotlin kickoff | [Status](implementation/status.md), [build/run guide](implementation/development.md), [validation](implementation/validation.md) | Delivery |
| Experience | [Interaction and accessibility](product/experience.md) | UX |
| Architecture | [System architecture index](system-architecture/README.md) | Architecture |
| Decisions | [ADR register](decisions/README.md) | Architecture |
| Delivery | [Roadmap](planning/roadmap.md), [backlog](planning/backlog.json) | Delivery |
| Risks | [Risk register](planning/risks.md) | Architecture / QA |
| Open choices | [Decision queue](planning/open-questions.md) | Product |
| Planning validation | [Checks and limitations](planning/validation-report.md) | Delivery |
| Agents | [Role handbook and handoffs](agents/README.md) | Delivery |
| Tooling | [REA plan](tooling/rea-setup-plan.md), [verification](tooling/rea-verification.md) | Tooling |
| Templates | [Task](templates/task.md), [ADR](templates/adr.md), [experiment](templates/experiment.md), [handoff](templates/handoff.md) | All roles |

## Maintaining the plan

Use stable requirement IDs (`FR-*`, `NFR-*`), task IDs (`PLAN-*`), risk IDs (`R-*`), and ADR IDs. A task is done only when its acceptance criteria have evidence. Updating a Markdown checkbox alone does not establish completion.

Change requirements and their downstream architecture, tests, and backlog together. External sources include a checked date and an explanation of what they support. Recheck version-dependent behavior before implementation and before release.

The machine-readable backlog is the task status source of truth. The roadmap groups those tasks into milestone gates; it does not promise calendar dates.
