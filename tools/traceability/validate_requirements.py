#!/usr/bin/env python3
# Copyright (c) 2026 Amit Chougule. All rights reserved.
"""Validate REQUIREMENTS.yml and check that every requirement tag used in tests exists.

Phase 0 guard rail. The full traceability matrix generator arrives in Phase 13.
Usage: python3 tools/traceability/validate_requirements.py
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
REQUIREMENTS = ROOT / "docs" / "regulatory" / "REQUIREMENTS.yml"
RISKS = ROOT / "docs" / "regulatory" / "RISK_ANALYSIS.md"

ID_PATTERN = re.compile(r"^REQ-(OPS|UI|RX|CDS|PHI|SEC|AUTH|INT)-\d{3}$")
REQUIRED_FIELDS = {"id", "title", "description", "rationale", "risk", "verification", "phase", "status"}
VERIFICATION = {"Test", "Inspection", "Analysis", "Demonstration"}
STATUS = {"planned", "in-progress", "implemented"}

# Tag syntaxes from docs/regulatory/TEST_TAGGING.md.
TAG_PATTERNS = {
    ".java": re.compile(r'@Tag\("(REQ-[A-Z]+-\d{3})"\)'),
    ".py": re.compile(r'mark\.req\("(REQ-[A-Z]+-\d{3})"\)'),
    ".cs": re.compile(r'Trait\("Req",\s*"(REQ-[A-Z]+-\d{3})"\)'),
    ".cpp": re.compile(r"\bREQ_([A-Z]+)_(\d{3})"),
    ".ts": re.compile(r"\[(REQ-[A-Z]+-\d{3})\]"),
    ".tsx": re.compile(r"\[(REQ-[A-Z]+-\d{3})\]"),
}
SKIP_DIRS = {"node_modules", ".venv", "target", "build", "bin", "obj", "dist", ".git"}


def load_requirements() -> list[dict]:
    data = yaml.safe_load(REQUIREMENTS.read_text(encoding="utf-8"))
    return data["requirements"]


def known_risks() -> set[str]:
    return set(re.findall(r"^\| (RISK-\d{3}) \|", RISKS.read_text(encoding="utf-8"), re.M))


def validate(reqs: list[dict], risks: set[str]) -> list[str]:
    errors: list[str] = []
    seen: set[str] = set()
    for req in reqs:
        rid = req.get("id", "<missing id>")
        missing = REQUIRED_FIELDS - req.keys()
        if missing:
            errors.append(f"{rid}: missing fields {sorted(missing)}")
        if not ID_PATTERN.match(rid):
            errors.append(f"{rid}: id does not match {ID_PATTERN.pattern}")
        if rid in seen:
            errors.append(f"{rid}: duplicate id")
        seen.add(rid)
        if req.get("verification") not in VERIFICATION:
            errors.append(f"{rid}: verification must be one of {sorted(VERIFICATION)}")
        if req.get("status") not in STATUS:
            errors.append(f"{rid}: status must be one of {sorted(STATUS)}")
        for risk in req.get("risk") or []:
            if risk not in risks:
                errors.append(f"{rid}: unknown risk {risk} (not in RISK_ANALYSIS.md)")
    return errors


def test_tags() -> dict[str, list[str]]:
    """Map each requirement ID found in test sources to the files that use it."""
    found: dict[str, list[str]] = {}
    for path in ROOT.rglob("*"):
        if path.suffix not in TAG_PATTERNS or SKIP_DIRS & set(path.parts):
            continue
        rel = path.relative_to(ROOT).as_posix()
        if "test" not in rel.lower():
            continue
        for match in TAG_PATTERNS[path.suffix].finditer(path.read_text(encoding="utf-8")):
            rid = f"REQ-{match.group(1)}-{match.group(2)}" if path.suffix == ".cpp" else match.group(1)
            found.setdefault(rid, []).append(rel)
    return found


def main() -> int:
    reqs = load_requirements()
    errors = validate(reqs, known_risks())
    ids = {r["id"] for r in reqs}
    tags = test_tags()
    for rid, files in sorted(tags.items()):
        if rid not in ids:
            errors.append(f"unknown requirement {rid} tagged in {sorted(set(files))}")
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    print(f"{len(reqs)} requirements valid; {len(tags)} referenced by tests: {', '.join(sorted(tags))}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
