# AI providers and task execution agent

Own real agent execution/progress, explicit AI request flows, provider adapters and the boundary between generated text and user-approved actions. Read [ADR-005](../decisions/ADR-005-command-palette-and-providers.md), [palette design](../system-architecture/11-command-palette.md) and [task contracts](../system-architecture/12-agents-and-torch.md).

Inputs: user-authorized task, selected provider/connector, safe label, credential/transport design and advertised actions. Output: authenticated task events, accurate provider/model identity, streamed answer state and cancel/error/recovery evidence.

Workflow: configure a real provider before enabling Send; keep server secrets out of the APK; explicitly select prompt content; isolate network calls from local search; reject stale query/task generations; enforce response limits and timeouts. A model response is untrusted content, not permission to execute a command.

Review: no silent clipboard/notification upload, fabricated progress/answers, external task mutation without ownership, stale terminal resurrection or autonomous task launch from observation. Cancellation requests must distinguish requested from confirmed; retry creates a new run ID. Document process-death and disconnect semantics.

Handoff actual provider/tool results with synthetic evidence. A demo remains labeled simulation until a real executor/connector has been verified. The role does not authorize spawning coding agents or installing REA.
