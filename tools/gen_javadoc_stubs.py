#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC_DIRS = [ROOT / 'src' / 'main' / 'java', ROOT / 'src' / 'test' / 'java']

class_re = re.compile(r'^(\s*)(public\s+)?(class|interface|enum)\s+(\w+)')
method_re = re.compile(r'^(\s*)(public|protected)\s+([\w\<\>\[\]]+)\s+(\w+)\s*\(([^)]*)\)\s*(?:throws [\w,\s]+)?\s*\{')
field_re = re.compile(r'^(\s*)(public|protected)\s+([\w\<\>\[\]]+)\s+(\w+)\s*(=.+)?;')

param_split_re = re.compile(r'(?:(?:final\s+)?[\w\<\>\[\]]+\s+)(\w+)')


def has_javadoc_before(lines, idx):
    i = idx - 1
    while i >= 0 and lines[i].strip() == '':
        i -= 1
    if i >= 0 and lines[i].strip().endswith('*/'):
        return True
    if i >= 0 and lines[i].strip().startswith('//'):
        return False
    return False


def make_class_javadoc(name, indent):
    return f"{indent}/**\n{indent} * {name} - TODO: describe this {name}\n{indent} */\n"


def make_field_javadoc(name, typ, indent):
    return f"{indent}/**\n{indent} * {name} ({typ}) - TODO: describe this field\n{indent} */\n"


def make_method_javadoc(name, ret_type, params, indent):
    lines = [f"{indent}/**", f"{indent} * {name} - TODO: describe this method"]
    if params.strip():
        parts = [p.strip() for p in params.split(',') if p.strip()]
        for p in parts:
            m = param_split_re.search(p)
            if m:
                pname = m.group(1)
            else:
                pname = 'param'
            lines.append(f"{indent} * @param {pname} TODO: describe parameter")
    if ret_type != 'void':
        lines.append(f"{indent} * @return TODO: describe return value")
    lines.append(f"{indent} */\n")
    return '\n'.join(lines)


def process_file(path: Path):
    text = path.read_text(encoding='utf-8')
    lines = text.splitlines()
    out = []
    i = 0
    changed = False
    while i < len(lines):
        line = lines[i]
        mclass = class_re.match(line)
        if mclass:
            indent = mclass.group(1)
            name = mclass.group(4)
            if not has_javadoc_before(lines, i):
                out.append(make_class_javadoc(name, indent))
                changed = True
            out.append(line)
            i += 1
            continue
        mfield = field_re.match(line)
        if mfield:
            indent = mfield.group(1)
            access = mfield.group(2)
            typ = mfield.group(3)
            name = mfield.group(4)
            if access in ('public','protected') and not has_javadoc_before(lines, i):
                out.append(make_field_javadoc(name, typ, indent))
                changed = True
            out.append(line)
            i += 1
            continue
        mmethod = method_re.match(line)
        if mmethod:
            indent = mmethod.group(1)
            access = mmethod.group(2)
            ret = mmethod.group(3)
            name = mmethod.group(4)
            params = mmethod.group(5)
            if access in ('public','protected') and not has_javadoc_before(lines, i):
                out.append(make_method_javadoc(name, ret, params, indent))
                changed = True
            out.append(line)
            i += 1
            continue
        out.append(line)
        i += 1
    if changed:
        path.write_text('\n'.join(out) + '\n', encoding='utf-8')
    return changed


def main():
    total = 0
    modified = 0
    for d in SRC_DIRS:
        if not d.exists():
            continue
        for path in d.rglob('*.java'):
            total += 1
            try:
                if process_file(path):
                    modified += 1
                    print(f"Patched: {path}")
            except Exception as e:
                print(f"Error processing {path}: {e}")
    print(f"Processed {total} files, modified {modified} files")

if __name__ == '__main__':
    main()
