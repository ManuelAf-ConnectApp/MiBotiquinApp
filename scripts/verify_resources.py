#!/usr/bin/env python3
"""Fail fast when localised Compose string keys or Room schema exports drift."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "commonResources/src/commonMain/composeResources"
DATABASE = ROOT / "data/src/commonMain/kotlin/com/connectapp/data/database/Database.kt"
SCHEMAS = ROOT / "data/schemas/com.connectapp.data.database.AppDatabase"

def keys(path):
    return {item.attrib["name"] for item in ET.parse(path).getroot().findall("string")}

reference = keys(RES / "values/strings.xml")
failed = False
for language in ("values-es", "values-en"):
    actual = keys(RES / language / "strings.xml")
    if actual != reference:
        failed = True
        print(f"{language}: missing={sorted(reference - actual)} extra={sorted(actual - reference)}")

version = int(re.search(r"version = (\d+)", DATABASE.read_text()).group(1))
schema = SCHEMAS / f"{version}.json"
if not schema.is_file():
    failed = True
    print(f"Missing Room schema export: {schema.relative_to(ROOT)}")
for current in range(14, version):
    if f"MIGRATION_{current}_{current + 1}" not in DATABASE.read_text():
        failed = True
        print(f"Missing migration {current}->{current + 1}")

if failed:
    sys.exit(1)
print(f"Resources match and Room v{version} schema is exported.")
