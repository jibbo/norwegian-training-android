#!/usr/bin/env python3
"""Verify that all supported locale string resources have identical keys."""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

SUPPORTED_LOCALES = ("values", "values-de", "values-es", "values-fr", "values-it", "values-zh")


def keys(path: Path) -> set[str]:
    root = ET.parse(path).getroot()
    return {element.attrib["name"] for element in root.findall("string")}


def main() -> int:
    res_dir = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
    files = {locale: res_dir / locale / "strings.xml" for locale in SUPPORTED_LOCALES}
    missing_files = [str(path) for path in files.values() if not path.is_file()]
    if missing_files:
        print("Missing resource files:")
        print("\n".join(missing_files))
        return 1

    english = keys(files["values"])
    failed = False
    for locale, path in files.items():
        locale_keys = keys(path)
        missing = sorted(english - locale_keys)
        extra = sorted(locale_keys - english)
        print(f"{locale}: {len(locale_keys)} keys")
        if missing:
            failed = True
            print(f"  missing: {', '.join(missing)}")
        if extra:
            failed = True
            print(f"  extra: {', '.join(extra)}")

    if failed:
        return 1
    print("All supported locale string keys match values/strings.xml.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
