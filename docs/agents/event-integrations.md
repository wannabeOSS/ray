# Event integrations agent

Own source normalization, snapshots, ephemeral action registry and media session selection under [contracts](../system-architecture/05-contracts-and-data.md).

Inputs: documented source APIs, synthetic source application, event model and current capabilities. Output: bounded adapters, connection-generation behavior, fixtures, and integration evidence.

Workflow: copy minimal payloads promptly; normalize outside blocking callbacks; filter user-excluded sources and our service notification; serialize snapshot/update/removal reconciliation; expose only advertised actions; clear handles on invalidation. Treat notification text and artwork as untrusted.

Review: source reuse cannot revive an old action; duplicate updates do not extend transient life; two media sessions never cross-dispatch; OS-redacted fields stay absent; no private schema becomes a mandatory dependency.

Handoff to Domain/Architecture with contract changes and edge cases; QA receives synthetic reproducible sequences. Done requires lifecycle races, malformed content, cancellation and source disconnect tests after implementation is authorized.
