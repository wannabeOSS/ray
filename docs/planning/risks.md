# Risk register

Owner: Architecture / QA · Status: initial assessment

Likelihood and impact are planning judgments. Reassess after each milestone.

| ID | Risk | Likelihood / impact | Mitigation and trigger | Owner / task |
| --- | --- | --- | --- | --- |
| R-01 | No defensible FGS/distribution path | Medium / critical | Evaluate before implementation; narrow experience if unsuitable | Platform + Product / PLAN-001, PLAN-004 |
| R-02 | OEM kills or suppresses overlay | High / high | Publish tested compatibility, explicit restart, useful degraded mode | Platform / PLAN-001, PLAN-008 |
| R-03 | Overlay obstructs input/system UI | Medium / critical | Bounded window, safe geometry, touch tests | UX + Platform / PLAN-001, PLAN-003 |
| R-04 | Private content leaks during lifecycle race | Medium / critical | Non-droppable guards, clear memory, action generations | Privacy / PLAN-004, PLAN-007 |
| R-05 | Notification format assumptions break | High / medium | Generic typed metadata; no parsing-dependent live activity claims | Integrations / PLAN-002 |
| R-06 | Battery or jank exceeds target | Medium / high | Event-driven operation, bounded caches, paired measurements | QA / PLAN-008 |
| R-07 | TalkBack cannot reliably use overlay | Medium / high | Early physical-device spike; in-app expanded fallback | UX / PLAN-003 |
| R-08 | Stale source action executes | Medium / high | Revision checks and final capability validation; no retries | Integrations / PLAN-006 |
| R-09 | REA starts with unsupported Node | Observed shell mismatch / medium | Bundled Node 24 runtime plus persistent scoped launch config; verify restart | Tooling / PLAN-010 |
| R-10 | Design scope exceeds public APIs | High / high | Keep calls/timers/navigation as discovery, not MVP promises | Product / PLAN-002 |
| R-11 | Unchosen license/name delays distribution | Medium / medium | Resolve before public repository/release | Product / PLAN-009 |
| R-12 | New Android policy changes invalidate plan | Medium / high | Recheck official docs at kickoff and release | Platform / PLAN-004, PLAN-009 |

No risk is considered closed merely because a mitigation is written. Closure requires task evidence and a review date.

## Expanded scope risks

| ID | Risk | Mitigation / owner |
| --- | --- | --- |
| R-13 | Background clipboard history cannot match a desktop launcher | Explicit focused import; never bypass Android clipboard privacy / Platform |
| R-14 | AI credentials, prompts or task events leak | Explicit Send, authenticated connector ownership, no APK server secrets / Privacy |
| R-15 | Rates or lyrics are unavailable, stale or unlicensed | Timestamped real providers and honest unavailable states / Integrations |
| R-16 | Third-party shortcuts need launcher role/access | Capability spike; ordinary app launch works independently / Platform |
| R-17 | Torch state/ownership changes outside this app | Authoritative callbacks, no optimistic state, physical-device tests / Platform |

These risks remain open; provider and hardware features are staged in PLAN-013–PLAN-017.
