#!/usr/bin/env python3
"""Kontroll statik i burimeve Android për gjuhën shqipe."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

BASE = Path("app/src/main/res/values/strings.xml")
SQ = Path("app/src/main/res/values-sq/strings.xml")
FORMAT = re.compile(r"%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]")

def entries(filename):
    root = ET.parse(filename).getroot()
    strings = {}
    plurals = {}
    for e in root:
        if e.tag == "string" and e.attrib.get("translatable", "true") != "false":
            strings[e.attrib["name"]] = "".join(e.itertext())
        elif e.tag == "plurals":
            plurals[e.attrib["name"]] = {
                item.attrib["quantity"]: "".join(item.itertext())
                for item in e if item.tag == "item"
            }
    return strings, plurals

def placeholders(s):
    return sorted(FORMAT.findall(s))

def main():
    base, base_plural = entries(BASE)
    sq, sq_plural = entries(SQ)
    errors = []
    for label, a, b in (("string", base, sq), ("plurals", base_plural, sq_plural)):
        missing, extra = sorted(a.keys() - b.keys()), sorted(b.keys() - a.keys())
        if missing: errors.append(f"{label}: mungojnë {missing[:30]}")
        if extra: errors.append(f"{label}: të tepërt {extra[:30]}")
    for k in base.keys() & sq.keys():
        if placeholders(base[k]) != placeholders(sq[k]):
            errors.append(f"parametrat e string nuk përputhen: {k}")
    for k in base_plural.keys() & sq_plural.keys():
        bp, sp = base_plural[k], sq_plural[k]
        if set(bp) != set(sp):
            errors.append(f"shumësat nuk përputhen: {k}")
        for qty in bp.keys() & sp.keys():
            if placeholders(bp[qty]) != placeholders(sp[qty]):
                errors.append(f"parametrat e shumësit nuk përputhen: {k}/{qty}")
    print(f"Burime shqip: {len(sq)}/{len(base)} strings, {len(sq_plural)}/{len(base_plural)} plurals")
    if errors:
        print("\\n".join(errors[:100]), file=sys.stderr)
        return 1
    print("OK: çelësat dhe parametrat përputhen")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
