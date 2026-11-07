#!/usr/bin/env python3
"""Kontrollo të gjitha burimet XML dhe mbulimin e gjuhës shqipe në Eta3."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path("app/src/main/res")
FORMATS = re.compile(r"%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]")

def resources(dirname):
    strings, plurals = {}, {}
    for file in sorted((ROOT / dirname).glob("*.xml")):
        root = ET.parse(file).getroot()
        for item in root:
            if item.tag not in {"string", "plurals"}:
                continue
            if item.get("translatable", "true") == "false":
                continue
            name = item.get("name")
            if not name:
                continue
            target = strings if item.tag == "string" else plurals
            if name in target:
                raise ValueError(f"Çelës i dyfishtë {name} në {file}")
            if item.tag == "string":
                target[name] = (("".join(item.itertext())), item.get("formatted", "true"))
            else:
                target[name] = {
                    child.get("quantity"): "".join(child.itertext())
                    for child in item if child.tag == "item"
                }
    return strings, plurals

def placeholders(value):
    return sorted(FORMATS.findall(value))

def main():
    en, enp = resources("values")
    sq, sqp = resources("values-sq")
    errors = []
    for label, source, trans in (("string", en, sq), ("plurals", enp, sqp)):
        missing, extra = sorted(source.keys() - trans.keys()), sorted(trans.keys() - source.keys())
        if missing: errors.append(f"{label}: mungojnë {missing[:50]}")
        if extra: errors.append(f"{label}: të tepërt {extra[:50]}")
    for k in en.keys() & sq.keys():
        enval, enformat = en[k]
        sqval, sqformat = sq[k]
        if enformat != sqformat:
            errors.append(f"formatted nuk përputhet për {k}: {enformat} / {sqformat}")
        if placeholders(enval) != placeholders(sqval):
            errors.append(f"parametrat nuk përputhen për {k}")
    for k in enp.keys() & sqp.keys():
        e, q = enp[k], sqp[k]
        if set(e) != set(q):
            errors.append(f"shumësat nuk përputhen për {k}: {set(e)} / {set(q)}")
        for quantity in e.keys() & q.keys():
            if placeholders(e[quantity]) != placeholders(q[quantity]):
                errors.append(f"parametrat e shumësit nuk përputhen për {k}/{quantity}")
    print(f"Burimet shqip: {len(sq)}/{len(en)} strings, {len(sqp)}/{len(enp)} plurals")
    if errors:
        print("\n".join(errors[:80]), file=sys.stderr)
        return 1
    print("OK: Të gjitha burimet, atributet dhe vendmbajtësit përputhen")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
