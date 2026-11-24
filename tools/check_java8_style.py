from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1]
errors = []

for path in root.rglob("*.java"):
    text = path.read_text(encoding="utf-8")

    if re.search(r"\bvar\s+[A-Za-z_$]", text):
        errors.append(f"{path}: uses var")

    if "Optional<" in text or "Optional." in text:
        errors.append(f"{path}: uses Optional")

    if "->" in text:
        errors.append(f"{path}: uses lambda; explicit anonymous classes are preferred in this backport baseline")

if errors:
    print("\n".join(errors))
    sys.exit(1)

print("Java 8/style scan passed for all Java source files.")
