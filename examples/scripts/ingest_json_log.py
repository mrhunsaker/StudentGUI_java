"""Simple ingestion example for newline-delimited JSON logs from Graph Digitizer.

Reads logs/graph-digitizer.json by default and prints filtered entries.
Usage:
    python ingest_json_log.py --level ERROR
    python ingest_json_log.py --logger com.digitizer.core
"""
from __future__ import annotations
import argparse
import json
from pathlib import Path

def parse_args():
    p = argparse.ArgumentParser(description="Ingest Graph Digitizer JSON log")
    p.add_argument("--path", default="logs/graph-digitizer.json", help="Path to NDJSON log file")
    p.add_argument("--level", help="Filter by level (INFO, DEBUG, WARN, ERROR)")
    p.add_argument("--logger", help="Prefix filter for logger name")
    return p.parse_args()

def iter_events(path: Path):
    with path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                yield json.loads(line)
            except json.JSONDecodeError:
                continue  # skip malformed

def main():
    args = parse_args()
    path = Path(args.path)
    if not path.exists():
        print(f"Log file not found: {path}")
        return 1
    count = 0
    for event in iter_events(path):
        if args.level and event.get("level") != args.level:
            continue
        if args.logger and not event.get("logger", "").startswith(args.logger):
            continue
        print(f"{event.get('time')} {event.get('level'):>5} {event.get('logger')} - {event.get('message')}")
        count += 1
    print(f"\nDisplayed {count} events")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
