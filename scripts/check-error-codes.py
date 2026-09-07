#!/usr/bin/env python3
"""Contract check: every ArchForgeProjectModule used by an ErrorCode enum must be
documented in docs/specs/error-codes.md (M3.2).

Run from the ArchForge repo root:
    python3 scripts/check-error-codes.py docs/specs/error-codes.md
"""

import re
import sys
from pathlib import Path

MODULE_ENUM = (
    "archforge-common/archforge-common-error/src/main/java/"
    "com/lesofn/archforge/common/error/ArchForgeProjectModule.java"
)


def main() -> int:
    if len(sys.argv) != 2:
        print("usage: check-error-codes.py <path-to-error-codes.md>")
        return 2

    doc = Path(sys.argv[1]).read_text(encoding="utf-8")

    module_enum_src = Path(MODULE_ENUM).read_text(encoding="utf-8")
    declared_modules = set(
        re.findall(r"^\s{4}([A-Z][A-Z0-9_]+)\s*[,;(]", module_enum_src, re.M)
    )
    if not declared_modules:
        print("FAIL: could not parse ArchForgeProjectModule constants")
        return 1

    registered: dict[str, str] = {}
    for src in Path(".").rglob("*ErrorCode.java"):
        text = src.read_text(encoding="utf-8")
        if "implements ErrorCode" not in text:
            continue
        for match in re.findall(r"ErrorManager\.register\(\s*ArchForgeProjectModule\.(\w+)", text):
            registered.setdefault(match, str(src))

    missing_doc = sorted(m for m in declared_modules | set(registered) if m not in doc)

    problems = []
    for module in missing_doc:
        origin = registered.get(module, "declared in ArchForgeProjectModule")
        problems.append(f"  {module} ({origin}) is not documented in {sys.argv[1]}")

    if problems:
        print("FAIL: undocumented error-code modules:")
        print("\n".join(problems))
        return 1

    print(f"OK: {len(registered)} ErrorCode enums across modules all documented")
    return 0


if __name__ == "__main__":
    sys.exit(main())
