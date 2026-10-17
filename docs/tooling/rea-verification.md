# REA verification record

Recorded: 2026-10-17 · State: **preview complete; approval pending**

| Check | Result | Evidence |
| --- | --- | --- |
| npm metadata | Passed | Version 6.5.0, declared Node range and archive integrity retrieved |
| Published archive integrity | Passed | Computed SHA-512 matched registry metadata |
| Supported runtime available | Passed | Existing bundled Node reports v24.19.0 |
| Read-only setup preview | Passed | Exit 0; `status: planned`; `appliedActions: []` |
| Existing registration baseline | Absent | Codex CLI reports no MCP server named `rea` |
| Existing workflow baseline | Absent | Preview reports missing skill |
| Read-only scoped doctor baseline | Not ready, as expected | Exit 1; Node 24.19.0 healthy; Codex registration and skill missing; live server unknown |
| Setup applied | Not run | Awaiting user approval |
| Persistent Node/npx launch | Not verified | Scoped runtime adjustment is proposed |
| Post-install scoped doctor | Not run | Requires approved setup |
| MCP initialize and tool listing | Not run | Requires registered command |
| Tool available in this chat | Not verified | May require client reload after setup |
| Optional analysis engines | Unavailable in preview | No configured Ghidra/IDA/JADX provider; none installed by this task |

The dry-run's `healthy: false` is a baseline with missing registration/skill/providers, not an installation failure. It must not be reported as a successful installation either.

Raw preview was saved locally at `/tmp/island-rea-setup-plan.json`; npm download cache is `/tmp/island-rea-npm-cache`. They are temporary, may disappear, and are not project dependencies. The committed [sanitized preview](rea-setup-preview.json) retains only the selected actions and relevant readiness summary.

The separate read-only baseline command `doctor --client codex --skill --json` was also executed under Node 24.19.0, with output at `/tmp/island-rea-doctor-baseline.json`. Its failure status confirms the integration is not ready before installation. The package's catalog metadata is not evidence that tools are connected to this chat.

## Post-approval evidence to record

Record the user's approval, exact package version, command/environment, setup exit/result, changed paths, backup/recovery paths, scoped doctor findings, MCP handshake and catalog result, harmless tool response, and active-client visibility. Never paste unrelated client settings or secrets into this document.

If base integration is ready but optional engines are absent, say exactly that. If configuration is written but the current chat has not reloaded, say **configured; active-chat connection pending**.
