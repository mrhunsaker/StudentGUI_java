#!/usr/bin/env python3
"""Fix common markdown lint issues across repo:
- Replace '```text' with '```'
- Convert angle-bracketed URLs like <https://...> into markdown links [https://...](https://...)
- Save backups as .bak
"""
import re
from pathlib import Path

root = Path('.').resolve()
md_files = list(root.rglob('*.md'))
print(f'Found {len(md_files)} markdown files')
patched = []
url_re = re.compile(r'<(https?://[^>\s]+)>')
for p in md_files:
    try:
        text = p.read_text(encoding='utf-8')
    except Exception:
        continue
    orig = text
    text = text.replace('```text', '```')
    text = url_re.sub(r'[\1](\1)', text)
    if text != orig:
        bak = p.with_suffix(p.suffix + '.bak')
        p.write_text(orig, encoding='utf-8') if not bak.exists() else None
        # Write backup only if not exists
        p.with_suffix(p.suffix + '.bak')
        Path(str(p) + '.bak').write_text(orig, encoding='utf-8')
        p.write_text(text, encoding='utf-8')
        print('Patched:', p)
        patched.append(str(p))
print('Done. Patched files:', len(patched))
