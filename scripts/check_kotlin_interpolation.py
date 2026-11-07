#!/usr/bin/env python3
"""Find suspicious Kotlin interpolation where an uppercase constant absorbs translated words.

Example invalid Kotlin string: "$MAX_INPUT_TEXT_CHARSkaraktere".
Use "${MAX_INPUT_TEXT_CHARS} karaktere" instead.
"""
from pathlib import Path
import re
import sys

ROOT = Path("app/src/main/kotlin")
# Upper-case constant name followed directly by lower-case text:
# Kotlin interprets the entire run as the identifier, causing unresolved references.
PATTERN = re.compile(r"(?<!\\)\\$[A-Z][A-Z0-9_]{2,}[a-z][A-Za-z0-9_]*")
issues = []
for file in ROOT.rglob("*.kt"):
    for number, line in enumerate(file.read_text(encoding="utf-8").splitlines(), 1):
        if line.lstrip().startswith(("//", "*")):
            continue
        for match in PATTERN.finditer(line):
            issues.append(f"{file}:{number}: {match.group(0)}")
if issues:
    print("Interpolime të dyshimta Kotlin (kontrollo kufijtë e variablave):", file=sys.stderr)
    print("\n".join(issues[:100]), file=sys.stderr)
    sys.exit(1)
print("OK: nuk u gjetën interpolime me konstante të bashkuara me tekst")
