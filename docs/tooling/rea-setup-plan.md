# REA setup plan — awaiting approval

Prepared: 2026-10-17 · Selected client: Codex only · Package observed: `rea-agents@6.5.0`

**No setup changes have been applied.** The requested command was run in read-only preview mode. Its result is `planned`, with zero applied actions. See the [sanitized preview](rea-setup-preview.json) and [verification record](rea-verification.md).

## Purpose and scope

REA is a local reverse-engineering CLI and MCP server for authorized development research. It is separate from the Android app. This setup enables its base tools and bundled workflow instructions in Codex. It does not authorize analysis of any particular APK or install native/Android analysis engines.

## Observed environment

| Item | Observed value |
| --- | --- |
| Shell Node | 25.1.0 — outside REA's declared supported range |
| Compatible existing Node | 24.19.0 at `/Users/abhijit/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node` |
| npm | 11.6.2 from the existing nvm installation |
| REA version | 6.5.0 from npm metadata and downloaded package |
| Supported Node range | `^22.19.0 || ^24.11.0 || >=26.0.0` |
| Existing Codex registration | `codex mcp get rea --json` reported no server named `rea` |
| Workflow skill | Missing in the dry-run diagnostic |
| Analysis providers | No configured provider reported by the preview |

The downloaded archive's SHA-512 matched npm metadata:

```text
sha512-C6BG7LyrAdR74mSjertM6PrxJkcNZ2bfQNOJornPo9erDEp2tyPRVYPnV5Yodr+1RqewCQla68WulXlVnveD+A==
```

## Exact installer preview

| Proposed operation | Path | Detail |
| --- | --- | --- |
| Update Codex configuration | `/Users/abhijit/.codex/config.toml` | Add `rea` stdio MCP registration using `npx -y rea-agents@6.5.0 mcp`; preserve unrelated configuration |
| Back up existing config | `/Users/abhijit/.codex/config.toml.rea.backup` | Installer-selected backup; if existing snapshots differ, retain an additional recovery copy |
| Install REA workflow skill | `/Users/abhijit/.agents/skills/reverse-engineer-anything` | Bundled instructions and on-demand references matching the package |

The configuration is user-level and can affect other Codex projects. The shared skill directory can be discoverable by other compatible agents even though only Codex's MCP registration is selected. No Hopper, Ghidra, IDA, JADX provider, global npm package, or system Node upgrade is in this plan.

## Runtime adaptation included in this approval

The generated `npx` command alone could pick unsupported Node 25 when Codex restarts. After approved setup, configure **only the REA server entry** to use the existing absolute npx launcher and a PATH prefixed with the supported bundled Node directory:

```toml
# Proposed adjustment to the setup-generated REA entry, not a replacement file.
[mcp_servers.rea]
command = "/Users/abhijit/.nvm/versions/node/v25.1.0/bin/npx"
args = ["-y", "rea-agents@6.5.0", "mcp"]

[mcp_servers.rea.env]
PATH = "/Users/abhijit/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin:/Users/abhijit/.nvm/versions/node/v25.1.0/bin:/opt/homebrew/bin:/usr/bin:/bin"
```

Merge these fields into the generated entry, retaining setup timeouts and other valid fields. The npx script uses the first Node on this scoped PATH; its nvm directory name does not mean Node 25 will execute it. Verify this through a fresh child process before claiming readiness. The app-managed runtime can move or be replaced in a later app update; document the dependency and rerun health checks then. A dedicated stable Node installation would be a separate choice if this runtime proves unsuitable.

## Commands and execution order

Already executed, with package cache in `/tmp/island-rea-npm-cache` and dependency lifecycle scripts disabled:

```sh
PATH='/Users/abhijit/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin:/Users/abhijit/.nvm/versions/node/v25.1.0/bin:/opt/homebrew/bin:/usr/bin:/bin' \
npm_config_cache=/tmp/island-rea-npm-cache \
npm_config_ignore_scripts=true npm_config_omit=optional \
npx --yes rea-agents@latest setup --client codex --dry-run --json
```

Here `npx --yes` approved only downloading the package for preview. `--dry-run` prevented setup writes. Temporary download/cache files exist; REA is not yet registered or connected.

After user approval:

1. Recheck the latest version and setup preview. If version, destinations or actions materially differ from this plan, present the changed plan before applying it.
2. Verify existing configuration is readable and retain the installer backup/recovery paths. Record file hashes without exposing unrelated configuration values.
3. Run the user's requested `npx rea-agents@latest setup`, scoped with `--client codex`, under the supported runtime. Use REA's apply confirmation only after this plan is approved. For an automated apply the suffix is `setup --client codex --yes --json`; npm download confirmation and REA setup confirmation remain distinct.
4. Keep optional dependency scripts/engines out of this base setup unless the actual tool requires a separately reviewed change. Use the normal user npm cache for persistent package availability; the preview's temporary cache is not a durable installation path.
5. Apply the scoped runtime adaptation above, preserving unrelated configuration and generated timeout fields.
6. Run the verification sequence below and report actual results. Reload/restart the client only when necessary; do not terminate the user's active work without coordination.

## Verification contract

- Package reports the reviewed version under a supported Node runtime.
- `doctor --client codex --skill --json` verifies the selected registration and workflow identity. Report missing optional providers separately.
- `codex mcp get rea --json` shows the intended command, arguments and scoped environment.
- A fresh stdio MCP client initializes the registered command and lists tools; run a harmless read-only diagnostic if advertised by that catalog.
- The active Codex client exposes REA tools after reload. If reload cannot happen in this chat, mark this final step pending instead of claiming a live connection.
- Confirm unrelated MCP entries are unchanged and document backup/recovery paths.

## Rollback

If setup fails, inspect its rollback report first. Restore only the REA-related changes using the pre-change snapshot; do not overwrite concurrent edits to unrelated config. Remove only the newly installed REA-owned skill if it was absent before. A broad `rea uninstall` may affect other client registrations, so inspect scope before using it. Do not purge research evidence or delete unrelated caches.

The approval requested is for the two installer actions, their backups, and the scoped runtime adaptation. Installation and connection verification are intentionally pending because the user explicitly asked to approve the setup plan first.

References: [REA installation guide](https://github.com/morluto/rea/blob/main/docs/installation.md), [package metadata](https://registry.npmjs.org/rea-agents/latest), [official Codex MCP documentation](https://developers.openai.com/codex/mcp).
