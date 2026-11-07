#!/usr/bin/env python3
"""Kontrollo mbulimin e shqipes për burimet Android, pa varësi të jashtme."""
from pathlib import Path
from collections import Counter
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "app/src/main/res/values/strings.xml"
SQ = ROOT / "app/src/main/res/values-sq/strings.xml"
LOCALES = ROOT / "app/src/main/res/xml/locales_config.xml"
GRADLE = ROOT / "app/build.gradle.kts"
FORMAT = re.compile(r"%(?:[0-9]+\$)?[-+# 0,(]*[0-9]*(?:\.[0-9]+)?[a-zA-Z%]")

def tokens(text):
    return Counter(FORMAT.findall(text or ""))

def main():
    errors = []
    en = ET.parse(BASE).getroot()
    sq = ET.parse(SQ).getroot()
    source_strings = {x.attrib["name"]: x for x in en.findall("string") if x.attrib.get("translatable") != "false"}
    target_strings = {x.attrib["name"]: x for x in sq.findall("string")}
    if set(source_strings) != set(target_strings):
        errors.append(f"Çelësa që mungojnë: {sorted(set(source_strings)-set(target_strings))}; shtesë: {sorted(set(target_strings)-set(source_strings))}")
    if len(sq.findall("string")) != len(target_strings):
        errors.append("Çelësa string të dyfishuar")
    for name in sorted(set(source_strings) & set(target_strings)):
        source, target = source_strings[name], target_strings[name]
        if tokens("".join(source.itertext())) != tokens("".join(target.itertext())):
            errors.append(f"Formatuesit nuk përputhen: {name}")
        if source.attrib.get("formatted") != target.attrib.get("formatted"):
            errors.append(f"Attributi formatted nuk përputhet: {name}")
    source_plural = {x.attrib["name"]: x for x in en.findall("plurals")}
    target_plural = {x.attrib["name"]: x for x in sq.findall("plurals")}
    if set(source_plural) != set(target_plural):
        errors.append("Emrat e grupeve të shumësit nuk përputhen")
    for name in set(source_plural) & set(target_plural):
        si = {x.attrib["quantity"]: x.text for x in source_plural[name].findall("item")}
        ti = {x.attrib["quantity"]: x.text for x in target_plural[name].findall("item")}
        if set(si) != set(ti):
            errors.append(f"Kategoritë e shumësit nuk përputhen: {name}")
        for category in set(si) & set(ti):
            if tokens(si[category]) != tokens(ti[category]):
                errors.append(f"Formatuesit e shumësit nuk përputhen: {name}/{category}")
    config = ET.parse(LOCALES).getroot()
    tags = [x.attrib.get("{http://schemas.android.com/apk/res/android}name") for x in config]
    if "sq" not in tags: errors.append("sq mungon te locales_config.xml")
    if not re.search(r'localeFilters.*"sq"', GRADLE.read_text(encoding="utf-8")):
        errors.append("sq mungon te localeFilters")
    if errors:
        for error in errors: print("GABIM:", error, file=sys.stderr)
        return 1
    print(f"OK: shqip {len(target_strings)}/{len(source_strings)} tekste, {len(target_plural)} grupe shumësi, locale sq")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
