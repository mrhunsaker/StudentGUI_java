#!/usr/bin/env python3
"""Fix markdown spacing issues:
- Ensure blank line before and after fenced code blocks (```)
- Ensure blank line before list blocks (- , * , or numbered lists)

Backs up modified files as .bak
"""
import re
from pathlib import Path

root = Path('.').resolve()
md_files = list(root.rglob('*.md'))
print(f'Found {len(md_files)} markdown files')
patched = []

# Patterns
fence_start_re = re.compile(r'([^\n])\n(```)', flags=re.M)
fence_end_re = re.compile(r'```\n([^\n`])', flags=re.M)
# list: -, *, or numbered like '1.' followed by space
list_re = re.compile(r'([^\n])\n(?=(?:\s{0,3}[-\*]\s)|(?:\s{0,3}\d+\.\s))', flags=re.M)

for p in md_files:
    text = p.read_text(encoding='utf-8')
    orig = text
    # Add blank line before fenced code block if missing
    text = fence_start_re.sub(r'\1\n\n\2', text)
    # Add blank line after fenced code block if missing
    text = fence_end_re.sub(r'```\n\n\1', text)
    # Add blank line before lists if missing
    text = list_re.sub(r'\1\n\n', text)

    if text != orig:
        bak = Path(str(p) + '.bak')
        if not bak.exists():
            bak.write_text(orig, encoding='utf-8')
        p.write_text(text, encoding='utf-8')
        print('Patched spacing:', p)
        patched.append(str(p))

print('Done. Files patched:', len(patched))
