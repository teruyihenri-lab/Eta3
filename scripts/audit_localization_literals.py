#!/usr/bin/env python3
"""Raport për vargjet e mundshme të palokalizuara në Kotlin. Nuk ndryshon kod."""
from pathlib import Path
import re
from collections import Counter

ROOT = Path("app/src/main/kotlin/io/github/mangi/eta")
QUOTED_CJK = re.compile(r'"(?:[^"\\]|\\.)*[\u3400-\u9fff](?:[^"\\]|\\.)*"')
DIRECT_UI = re.compile(r'(?:Text|TextField|BasicText|EtaPreference|EtaTextButton)\\s*\\(\\s*(?:text\\s*=\\s*)?"[A-Za-z][^"]{2,}"')
counts = Counter()
candidates = []
for path in ROOT.rglob("*.kt"):
    if "/ui/" not in path.as_posix() and "/agent/voice/" not in path.as_posix():
        continue
    for n, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if line.lstrip().startswith("//"):
            continue
        categories = []
        if QUOTED_CJK.search(line):
            categories.append("KIN")
        if DIRECT_UI.search(line):
            categories.append("EN")
        if categories:
            key = "/".join(categories)
            counts[key] += 1
            candidates.append((key, path.as_posix(), n, line.strip()[:180]))
print("Raporti i literaleve të mundshme në ndërfaqe dhe zë:")
for k, n in counts.most_common():
    print(f"{k}: {n} rreshta")
for key, path, n, line in candidates[:240]:
    print(f"{key} {path}:{n}: {line}")
if len(candidates) > 240:
    print(f"... dhe {len(candidates)-240} rreshta të tjerë")
print("Ky është auditim orientues: kërkohet shqyrtim manual për tekstet e dukshme.")
