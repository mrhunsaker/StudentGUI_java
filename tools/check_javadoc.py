#!/usr/bin/env python3
import re
from pathlib import Path

root = Path('src/main/java')
if not root.exists():
    print('No src/main/java found')
    raise SystemExit(1)

class_re = re.compile(r'^(?P<prefix>\s*(public|protected|private|static|final|abstract|sealed|non-sealed)\s+)*\s*(class|interface|enum)\s+(?P<name>\w+)', re.MULTILINE)
method_re = re.compile(r'^(?P<prefix>\s*(public|protected)\s+)(?:[\w<>,\s\[\]]+\s+)+(?P<name>\w+)\s*\(', re.MULTILINE)

missing_classes = []
missing_methods = []

for path in root.rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    lines = text.splitlines()
    # find classes
    for m in class_re.finditer(text):
        start = m.start()
        # find line number
        lineno = text.count('\n', 0, start) + 1
        # check previous non-blank, non-annotation lines for /** */
        i = lineno - 2
        has_javadoc = False
        while i >= 0:
            line = lines[i].strip()
            if line == '':
                i -= 1
                continue
            if line.startswith('@'):
                i -= 1
                continue
            if line.endswith('*/'):
                # find start of javadoc
                j = i
                while j >= 0 and '/*' not in lines[j]:
                    j -= 1
                if j >= 0 and lines[j].strip().startswith('/**'):
                    has_javadoc = True
            break
        if not has_javadoc:
            missing_classes.append((path, lineno, m.group('name')))
    # find methods
    for m in method_re.finditer(text):
        start = m.start()
        lineno = text.count('\n', 0, start) + 1
        i = lineno - 2
        has_javadoc = False
        while i >= 0:
            line = lines[i].strip()
            if line == '':
                i -= 1
                continue
            if line.startswith('@'):
                i -= 1
                continue
            if line.endswith('*/'):
                j = i
                while j >= 0 and '/*' not in lines[j]:
                    j -= 1
                if j >= 0 and lines[j].strip().startswith('/**'):
                    has_javadoc = True
            break
        if not has_javadoc:
            missing_methods.append((path, lineno, m.group('name')))

print('Missing class Javadoc:')
for p, ln, name in missing_classes:
    print(f'{p}:{ln} -> {name}')

print('\nMissing public/protected method Javadoc (first 200):')
for p, ln, name in missing_methods[:200]:
    print(f'{p}:{ln} -> {name}')

print('\nSummary:')
print(f'  files scanned: {sum(1 for _ in root.rglob("*.java"))}')
print(f'  missing classes: {len(missing_classes)}')
print(f'  missing methods: {len(missing_methods)}')
