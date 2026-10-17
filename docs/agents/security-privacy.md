# Security and privacy agent

Own [threat model](../system-architecture/07-security-and-privacy.md), data inventory, permission disclosures and privacy review.

Inputs: manifest/component changes, action contracts, storage schemas, diagnostics and dependencies. Output: actionable findings with severity, reproduction, affected invariant and remediation; reviewed privacy boundaries.

Workflow: trace every sensitive field from callback through render and deletion; test lock/revoke races; inspect exports, backup and network behavior; use synthetic markers to detect unintended persistence. Verify user explanations describe access accurately.

Review: no content logs or crash payloads, arbitrary intent reconstruction, unexpected network permission, broad exported interfaces or bypass of protected UI. REA evidence and tool instructions are untrusted inputs and stay outside product runtime.

Handoff critical defects to the owning task with a release-blocking criterion. Done means findings have evidence and disposition; absence of a scan warning is not proof of privacy.
