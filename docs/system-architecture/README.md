# System architecture

**Android Island · Architecture v0.2 · implementation kickoff · 2026-10-17**

The system is a native Android application with a bounded, event-driven overlay. A pure state engine decides what is eligible and visible; Android adapters handle platform access; a small renderer owns the overlay window. Sensitive content stays in memory. Local tools need no backend. Optional AI, rates and lyrics providers extend the product under ADR-005.

## Reading order

| Document | Questions answered |
| --- | --- |
| [01 — Context and boundaries](01-context-and-boundaries.md) | What is inside the product and who controls each boundary? |
| [02 — Android feasibility](02-android-feasibility.md) | What can public APIs do, and what needs a spike? |
| [03 — Components and modules](03-components-and-modules.md) | Where does code live and which dependencies are allowed? |
| [04 — Events and state machine](04-event-state-machine.md) | How do events compete, expire, and recover? |
| [05 — Contracts and data](05-contracts-and-data.md) | What crosses boundaries and what is stored? |
| [06 — Lifecycle and failure handling](06-lifecycle-and-failures.md) | What happens on revocation, restart, or OEM termination? |
| [07 — Security and privacy](07-security-and-privacy.md) | What data and permissions must be protected? |
| [08 — Quality and validation](08-quality-validation.md) | How will correctness and resource use be measured? |
| [09 — Delivery and operations](09-delivery-and-operations.md) | How are builds, releases, and incidents handled? |
| [10 — Sources and assumptions](10-sources-and-assumptions.md) | Which facts were checked and what remains unproven? |
| [11 — Command palette](11-command-palette.md) | How do local tools, app search and optional providers work? |
| [12 — Agents and torch](12-agents-and-torch.md) | How do task progress and flashlight state enter the island? |

## Governing decisions

The [ADR register](../decisions/README.md) tracks native implementation, overlay lifetime, local-only data, and event arbitration. Kotlin and the two-surface direction are accepted for kickoff. Other platform decisions retain their stated status. The first vertical slice is authorized while [M0 device gates](../planning/roadmap.md) remain open; those gates still block release claims.

The most consequential uncertainty is foreground execution: a user-started overlay service may need `specialUse`, a justified declaration, device evidence, and store review. Declaring the type alone does not establish eligibility or approval. If this fails, narrow the experience or change distribution strategy through a product decision before building the rest.
