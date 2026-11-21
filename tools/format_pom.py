#!/usr/bin/env python3
"""Apply or check the repository's deterministic two-space POM formatting."""

import argparse
import difflib
import sys
from pathlib import Path
from xml.dom import Node, minidom


POM = Path(__file__).resolve().parent.parent / "pom.xml"


def formatted_pom(source: str) -> str:
    document = minidom.parseString(source)

    def remove_indentation(node: Node) -> None:
        for child in list(node.childNodes):
            if child.nodeType == Node.TEXT_NODE and not child.data.strip():
                node.removeChild(child)
                child.unlink()
            elif child.nodeType == Node.ELEMENT_NODE:
                remove_indentation(child)

    remove_indentation(document)
    return document.toprettyxml(indent="  ", encoding="UTF-8").decode("UTF-8")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--check", action="store_true", help="fail if the POM is not formatted")
    mode.add_argument("--write", action="store_true", help="rewrite the POM in the required format")
    args = parser.parse_args()
    original = POM.read_text(encoding="UTF-8")
    expected = formatted_pom(original)
    if args.write:
        POM.write_text(expected, encoding="UTF-8")
        return 0
    if original == expected:
        return 0
    diff = difflib.unified_diff(
        original.splitlines(keepends=True),
        expected.splitlines(keepends=True),
        fromfile="pom.xml",
        tofile="formatted pom.xml",
    )
    sys.stderr.writelines(diff)
    print("Run: python3 tools/format_pom.py --write", file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
