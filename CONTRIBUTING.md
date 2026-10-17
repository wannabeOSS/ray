# Contributing

This workspace has entered Kotlin implementation kickoff. Read [AGENTS.md](AGENTS.md) and the [documentation map](docs/README.md) first.

## Changes

Choose a task from [the backlog](docs/planning/backlog.json). Keep requirements, architecture, acceptance criteria and risks aligned. Change architectural decisions through [ADRs](docs/decisions/README.md). Use the [task template](docs/templates/task.md) for new work.

Before review, run:

```sh
make docs-check
```

Use Python 3.11 or later. There are no Python/npm dependencies for documentation checks. If `make` is unavailable, run `python3 scripts/check_docs.py`.

## Review expectations

Explain the problem, resulting behavior, evidence and unresolved risks. Do not claim Android builds, tests, device support or CI results that have not run. Route review using the [agent handbook](docs/agents/README.md). Small documentation edits need proportionate review, not a new approval ceremony.

The initial Kotlin app is a development slice; physical-device and release milestone gates still apply. Follow the [Android development guide](docs/implementation/development.md) for build/test commands. Do not install REA until the user's requested setup-plan approval is recorded.

## Data and licensing

Use synthetic notifications in examples and tests. Keep raw research, APKs, HAR captures, credentials, signing keys and local logs out of Git. No project license has been selected; do not add an arbitrary license or represent the project as open source until the owner decides.
