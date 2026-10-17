# Open decisions

The following defaults allow planning to proceed. They are proposals, not confirmed user preferences.

| Question | Working default | Needed by | Owner |
| --- | --- | --- | --- |
| Product/repository name and app namespace? | Android Island codename; provisional dev.androidisland namespace for the kickoff | Scaffold / public naming | Product |
| Which audience and minimum Android version? | API 29+ phone pilot | PLAN-001 | Product |
| Which exact test phones are available? | Pixel, Samsung, one additional OEM | PLAN-001 | QA |
| Initial distribution channel? | Evaluate Google Play eligibility; no commitment yet | PLAN-004 | Product |
| Should previews show message text by default? | No; explicit opt-in | PLAN-003 | Product / Privacy |
| Landscape/foldables at launch? | Hide landscape by default; fold support conditional on testing | PLAN-003 | UX |
| Is persistent mode acceptable with an ongoing notification? | Yes as proposed; test onboarding comprehension | PLAN-003 | Product |
| License and commercial model? | No license granted or monetization implemented yet | Public distribution | Product |
| Install REA's Codex integration and workflow skill? | Await explicit approval of the completed setup plan | PLAN-010 | User |
| Need APK/native reverse-engineering engines? | No target or engine installation included | Future research request | User / Tooling |

A decision can be accepted in conversation and then recorded here/ADRs. Avoid repeatedly asking settled questions. Do not block independent documentation work on optional product choices.

## Choices added by the expanded product

Kotlin implementation and AI-agent task meaning are confirmed. Still to resolve: which real AI executor/provider and external connector, credential/hosting model, fiat/crypto rate source, authorized lyrics source, full home-launcher role versus ordinary invocation, and exact pilot phones. The first local build proceeds without silently choosing paid services or embedding model keys. REA setup approval remains a separate unresolved choice.
