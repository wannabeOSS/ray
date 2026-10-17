# Product requirements

Status: scope expanded for Kotlin kickoff · Owner: Product · Updated: 2026-10-17

## Problem and outcome

Users want a Dynamic Island-style live activity surface plus a Raycast-inspired command palette. Music, AI agents executing tasks and flashlight state are explicit requested island activities. The palette brings app launch/search, local tools and optional AI into the same product. The experience should feel quick, calm, and reversible. The app must accurately explain that Android retains control of overlay placement, notification visibility, and background execution.

Primary user: an Android phone owner who deliberately enables the feature and chooses which apps may contribute. Initial deployment: a small pilot on explicitly tested phones. Final public name, business model, distribution channel, and pilot models remain open.

## Scope

| Requirement | MVP behavior | Acceptance evidence |
| --- | --- | --- |
| FR-01 | Explain permissions, show an in-app synthetic preview, and request each access only in context | First-run test succeeds with every permission denied |
| FR-02 | User starts, pauses, resumes, and stops the overlay | Stop removes the window and service; restart is deliberate |
| FR-03 | Show eligible notification metadata from user-selected apps | Posting, updating, grouping, removing, and dismissing synthetic notifications follow the event contract |
| FR-04 | Expand an event and invoke a supported source action | Action is dispatched once after current capability checks; canceled actions fail safely |
| FR-05 | Show available media state and supported transport controls | Two-session handoff and session termination tested; unsupported controls are absent |
| FR-06 | Show a brief charging-connected/disconnected event | One event per state transition; no repetitive battery-level polling |
| FR-07 | Adjust position, size, theme, motion, preview privacy, and source allowlist | Valid values survive restart; invalid stored values recover to defaults |
| FR-08 | Hide content on lock/screen-off and clear it on permission loss | Privacy transitions tested while expanded and while an action is pending |
| FR-09 | Explain unavailable permissions, disconnected listener, and stopped service | Each state has a readable recovery path without a permission loop |
| FR-10 | Support touch, TalkBack, large text, RTL, and reduced motion | QA matrix and experience criteria pass on pilot devices |
| FR-11 | Show AI agent task states and progress in compact/expanded island; support advertised cancel/retry | Stale revisions and terminal transitions tested; simulations clearly labeled; real connector authenticated |
| FR-12 | Observe/control flashlight with actual torch callbacks | Permission denial, no flash, camera contention and external toggles handled; no optimistic hardware state |
| FR-13 | Focused universal palette searches launchable apps and commands | Stable component identity, alias matches, query cancellation and unavailable app behavior tested |
| FR-14 | Favorites, recent launches and grid/list display | Favorites/layout persist; recent contains only launches through this app; clear/opt-out available |
| FR-15 | Calculator, multiple function graphs and offline unit conversion | Safe bounded parser; screenshot graph expression works; invalid/undefined/discontinuous functions handled |
| FR-16 | Fiat/crypto conversion, suggestions and historical chart | Real configured provider, pair, timestamps and stale/offline state; no fabricated price history |
| FR-17 | Explicit Ask AI and inline streamed answers with provider/model identity | Query sent only on explicit action; cancellation/error and secret handling validated |
| FR-18 | Expanded media artwork, lyrics and synchronization offset | Supported playback controls only; authorized lyrics source and unavailable state; bounded artwork |
| FR-19 | Searchable emoji with categories and copy | Accessible names, gesture category and Unicode-safe copy |
| FR-20 | Search/import clipboard, timestamps/count, pause/resume, copy/delete/clear | Explicit focused text import only; reject sensitive clips; bounded memory and lock clearing |
| FR-21 | Per-app priority, visibility, favorites, aliases and shortcut inclusion | Hidden apps restored from management; stable preferences; shortcut permission limitations explained |
| FR-22 | Clear/back, result count, action menu, date footer and accessible invocation | Touch-first flows, optional hardware keyboard, no assumed global hotkeys |
| FR-23 | Optional online providers are isolated from private sources | No implicit clipboard/notification upload; correct offline/disabled behavior and data disclosure |

Calls, system timers, navigation, delivery tracking, and arbitrary third-party live activities are **later discovery candidates**. Notification text alone is not a reliable integration contract. No claim of universal app support, exact camera-hole placement, status-bar replacement, or guaranteed always-on operation.

Excluded from the current kickoff: notification history, cloud sync, user accounts, ads, purchases, automatic replies, arbitrary scripts, accessibility-service monitoring, call interception, privileged APIs and reverse-engineered private integrations. AI answers, real task execution, rates and lyrics are requested product features with staged provider work, not implemented services. See the [complete reference map](raycast-feature-map.md).

## Quality requirements

| ID | Proposed release criterion | Measurement |
| --- | --- | --- |
| NFR-01 | Notification content never persists or leaves the device | Storage/log inspection and network capture with synthetic data |
| NFR-02 | Eligible callback to first displayed frame p95 ≤150 ms | Trace adapter receipt through frame presentation; ≥100 events per pilot model |
| NFR-03 | <1% janky frames in the defined interaction benchmark | Frame timing over repeated compact/expanded transitions; report refresh rate |
| NFR-04 | Incremental idle battery cost ≤1 percentage point over 8 hours | Paired baseline/enabled runs, ≥3 pairs under fixed conditions; report uncertainty |
| NFR-05 | No uncaught crash, stale window, or content leak in the lifecycle suite | Rotation, process death, permission revocation, lock, and force-stop scenarios |
| NFR-06 | Permission denial preserves settings and in-app preview | Automated navigation plus physical-device checks |
| NFR-07 | Bounded resource use during notification floods | Queue, cache, memory and drop metrics; no main-thread stalls or unbounded growth |
| NFR-08 | Local app/command result update p95 ≤100 ms for 500 indexed entries | Device benchmark with typing/query changes; expensive providers canceled off main thread |
| NFR-09 | Provider isolation and content privacy | Explicit-send tests, canceled response generation checks, credential/transport audit |

These are targets, not measured achievements. QA may revise budgets by ADR after baseline profiling. Release is blocked if privacy or lifecycle criteria fail even when animation targets pass.

## Product acceptance

Product acceptance requires FR-01–FR-23 evidence, including staged screenshot features, NFR results with device context, and resolution of the foreground-service and distribution gates. Beta feedback is collected manually with explicit user consent and synthetic reproduction steps; the MVP has no telemetry backend.


The first Kotlin slice is a kickoff milestone, not completion of the entire acceptance table. [Implementation status](../implementation/status.md) records source/build evidence separately from planned functionality.
