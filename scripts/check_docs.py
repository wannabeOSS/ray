#!/usr/bin/env python3
"""Validate the planning workspace using only the Python standard library.

Checks repository-local inline Markdown file links (not web URLs or fragments),
required entry points, ADR status, and backlog identity/dependency/evidence rules.
This is intentionally not a Markdown renderer or a general YAML validator.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit

ROOT = Path(__file__).resolve().parents[1]
EXCLUDED = {".git", ".local", ".firecrawl", ".rea", "node_modules", "__pycache__"}
REQUIRED = (
    "README.md", "AGENTS.md", "CONTRIBUTING.md", "SECURITY.md", "docs/README.md",
    "docs/AGENTS.md", "docs/product/requirements.md", "docs/product/experience.md",
    "docs/system-architecture/README.md", "docs/agents/README.md",
    "docs/decisions/README.md", "docs/planning/backlog.json",
    "docs/planning/roadmap.md", "docs/planning/risks.md",
    "docs/planning/open-questions.md", "docs/tooling/rea-setup-plan.md",
    "docs/tooling/rea-verification.md", "docs/tooling/rea-setup-preview.json",
    "docs/templates/task.md", "docs/templates/adr.md",
    "docs/templates/experiment.md", "docs/templates/handoff.md",
    ".github/workflows/docs.yml", "Makefile",
    "docs/product/raycast-feature-map.md", "docs/system-architecture/11-command-palette.md",
    "docs/system-architecture/12-agents-and-torch.md", "docs/implementation/status.md",
    "docs/implementation/development.md", "settings.gradle.kts", "app/build.gradle.kts",
    "core/domain/build.gradle.kts", "gradle/libs.versions.toml",
)


def without_fences(text: str) -> str:
    lines = []
    fence = None
    for line in text.splitlines():
        marker = re.match(r"^\s*(`{3,}|~{3,})", line)
        if marker:
            token = marker.group(1)
            if fence is None:
                fence = token
            elif token[0] == fence[0] and len(token) >= len(fence):
                fence = None
            lines.append("")
        else:
            lines.append(line if fence is None else "")
    return "\n".join(lines)


def check() -> int:
    errors: list[str] = []
    for name in REQUIRED:
        if not (ROOT / name).is_file():
            errors.append(f"Missing required file: {name}")

    markdown = sorted(
        p for p in ROOT.rglob("*.md")
        if not (set(p.relative_to(ROOT).parts) & EXCLUDED)
    )
    checked_links = 0
    # Repository documentation uses inline links with plain or angle-bracket paths.
    link_pattern = re.compile(r"!?\[[^\]\n]*\]\((<[^>]+>|[^\s)]+)(?:\s+\"[^\"]*\")?\)")
    for path in markdown:
        text = path.read_text(encoding="utf-8")
        relative = path.relative_to(ROOT)
        if not text.strip():
            errors.append(f"Empty document: {relative}")
        if text and not text.endswith("\n"):
            errors.append(f"Missing final newline: {relative}")
        for match in link_pattern.finditer(without_fences(text)):
            target = match.group(1).strip("<>")
            parsed = urlsplit(target)
            if parsed.scheme or parsed.netloc or not parsed.path:
                continue
            checked_links += 1
            destination = (path.parent / unquote(parsed.path)).resolve()
            if not destination.is_relative_to(ROOT):
                errors.append(f"Link escapes repository: {relative}: {target}")
            elif not destination.exists():
                errors.append(f"Broken local link: {relative}: {target}")

    adr_paths: dict[str, Path] = {}
    for path in (ROOT / "docs/decisions").glob("ADR-*.md"):
        match = re.match(r"(ADR-\d{3})-", path.name)
        if not match:
            errors.append(f"Invalid ADR filename: {path.name}")
            continue
        key = match.group(1)
        if key in adr_paths:
            errors.append(f"Duplicate ADR ID: {key}")
        adr_paths[key] = path
        if not re.search(r"^Status: (proposed|accepted|superseded|rejected)\b", path.read_text(), re.M):
            errors.append(f"Missing or invalid ADR status: {path.name}")

    try:
        backlog = json.loads((ROOT / "docs/planning/backlog.json").read_text())
    except (OSError, ValueError) as exc:
        errors.append(f"Cannot read backlog: {exc}")
        backlog = {}
    tasks = backlog.get("tasks", [])
    if backlog.get("schema_version") != 1 or not isinstance(tasks, list) or not tasks:
        errors.append("Backlog must have schema_version 1 and a nonempty tasks list")
        tasks = []
    statuses = {"proposed", "ready", "in_progress", "blocked", "done"}
    milestones = {"P0", "M0", "M1", "M2", "M3", "M4", "T0"}
    requirements_path = ROOT / "docs/product/requirements.md"
    requirement_ids = set(re.findall(r"\b(?:FR|NFR)-\d{2}\b", requirements_path.read_text())) if requirements_path.exists() else set()
    by_id = {}
    for task in tasks:
        if not isinstance(task, dict):
            errors.append("Backlog task must be an object")
            continue
        key = task.get("id", "")
        if not isinstance(key, str) or not re.fullmatch(r"PLAN-\d{3}", key):
            errors.append(f"Invalid task ID: {key!r}")
            continue
        if key in by_id:
            errors.append(f"Duplicate task ID: {key}")
        by_id[key] = task
        for field in ("title", "owner", "summary"):
            if not isinstance(task.get(field), str) or not task[field].strip():
                errors.append(f"{key}: missing {field}")
        if task.get("status") not in statuses:
            errors.append(f"{key}: invalid status")
        if task.get("milestone") not in milestones:
            errors.append(f"{key}: invalid milestone")
        for field in ("depends_on", "requirements", "adrs", "acceptance", "documents", "evidence"):
            values = task.get(field)
            if not isinstance(values, list) or not all(isinstance(x, str) and x.strip() for x in values):
                errors.append(f"{key}: {field} must be a list of nonempty strings")
                task[field] = []
        if not task["acceptance"]:
            errors.append(f"{key}: missing acceptance criteria")
        if not task["documents"]:
            errors.append(f"{key}: missing document references")
        for name in task["documents"] + task["evidence"]:
            target = (ROOT / name).resolve()
            if not target.is_relative_to(ROOT) or not target.is_file():
                errors.append(f"{key}: missing or invalid repository file: {name}")
        for reference in task["adrs"]:
            if reference not in adr_paths:
                errors.append(f"{key}: unknown ADR: {reference}")
        for reference in task["requirements"]:
            if reference not in requirement_ids:
                errors.append(f"{key}: unknown requirement: {reference}")
        if task.get("status") == "done" and not task["evidence"]:
            errors.append(f"{key}: done task requires evidence")
        if task.get("status") == "blocked" and not task.get("blocker"):
            errors.append(f"{key}: blocked task requires a blocker explanation")

    visiting: set[str] = set()
    visited: set[str] = set()

    def visit(key: str) -> None:
        if key in visiting:
            errors.append(f"Dependency cycle includes {key}")
            return
        if key in visited:
            return
        visiting.add(key)
        for dependency in by_id[key]["depends_on"]:
            if dependency not in by_id:
                errors.append(f"{key}: unknown dependency: {dependency}")
            else:
                if by_id[key].get("status") == "done" and by_id[dependency].get("status") != "done":
                    errors.append(f"{key}: done task has unfinished dependency {dependency}")
                visit(dependency)
        visiting.remove(key)
        visited.add(key)

    for key in by_id:
        visit(key)
    covered = {ref for task in by_id.values() for ref in task["requirements"]}
    for missing in sorted(requirement_ids - covered):
        errors.append(f"Requirement has no backlog task: {missing}")

    if errors:
        print("Planning checks failed:")
        for error in errors:
            print(f"- {error}")
        return 1
    print(f"PASS: {len(markdown)} Markdown files, {checked_links} local file links, "
          f"{len(by_id)} tasks, {len(adr_paths)} ADRs, {len(requirement_ids)} requirements.")
    print("Not checked: external URLs, link fragments, Mermaid rendering, Android behavior or hosted CI.")
    return 0


if __name__ == "__main__":
    if sys.version_info < (3, 11):
        raise SystemExit("Python 3.11+ is required")
    raise SystemExit(check())
