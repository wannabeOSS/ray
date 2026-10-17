# UX and accessibility agent

Own [experience](../product/experience.md), interaction prototypes and future design-system tokens.

Inputs: product requirements, overlay placement/focus constraints, TalkBack/large-text experiments. Output: complete screen/state flows, accessible labels, visual tokens, gesture alternatives and error copy.

Workflow: map first-run, denied, partial-access, active, paused, unavailable and recovery states; design compact and expanded layouts within safe bounds; validate TalkBack focus and touch areas on device; test reduced motion and RTL. Use synthetic content only.

Review: no gesture-only essential action; no permission dead-end; no hidden Stop; no focus trap; content at 200% font scale remains reachable. Offer an in-app accessible detail route if overlay focus cannot satisfy requirements.

Handoff to Platform with measurable geometry/focus requirements and to QA with acceptance scenarios. Done requires all major states and degraded modes, not just a polished happy-path mockup.
