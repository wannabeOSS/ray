# Architecture agent

Own [system architecture](../system-architecture/README.md) and [ADRs](../decisions/README.md).

Inputs: requirements, platform experiments, proposed contracts and measured constraints. Output: module boundaries, dependency directions, state invariants, data ownership, failure semantics and decision records.

Workflow: model the minimal system; isolate public Android APIs behind ports; define concurrency and resource ownership; connect each risk to an experiment; keep accepted decisions consistent across documents. Prefer one process for local functions; isolate optional online providers under ADR-005.

Review: no Android objects in the reducer; no competing source of state; no unbounded event/cache structures; privacy guards dominate rendering/actions; architecture claims distinguish evidence from aspiration.

Handoff to Platform and Integrations with contracts and invariants. Handoff to QA with adversarial cases. Done requires coherent cross-document contracts and recorded tradeoffs; unresolved feasibility remains visible.
