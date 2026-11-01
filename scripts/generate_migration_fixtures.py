#!/usr/bin/env python3
"""Copy exported historical Room DDL into portable Kotlin test fixtures."""
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TARGET = ROOT / 'data/src/sqliteTest/kotlin/com/connectapp/data/database/HistoricalSchemas.kt'


def render():
    lines = ['// Generated from checked-in Room schemas by scripts/generate_migration_fixtures.py.',
             '// Do not edit SQL or identity hashes here; regenerate from the original exports.',
             'package com.connectapp.data.database', '',
             'internal fun historicalSchema(version: Int): List<String> = when (version) {']
    for version in (1, 2, 3, 4, 5, 6, 7, 8):
        schema = json.loads((ROOT / f'data/schemas/com.connectapp.data.database.AppDatabase/{version}.json').read_text())['database']
        statements = []
        for entity in schema['entities']:
            statements.append(entity['createSql'].replace('${TABLE_NAME}', entity['tableName']))
            statements.extend(index['createSql'].replace('${TABLE_NAME}', entity['tableName']) for index in entity.get('indices', []))
        statements.extend(schema['setupQueries'])
        statements.append(f'PRAGMA user_version = {version}')
        lines.append(f'    {version} -> listOf(')
        lines.extend('        """' + sql + '""",' for sql in statements)
        lines.append('    )')
    lines.extend(['    else -> error("No exported fixture for version $version")', '}', ''])
    return '\n'.join(lines)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    expected = render()
    if args.check:
        if not TARGET.exists() or TARGET.read_text() != expected:
            raise SystemExit('Migration fixtures are stale; run scripts/generate_migration_fixtures.py')
        print('Migration fixtures match the exported Room schemas.')
    else:
        TARGET.parent.mkdir(parents=True, exist_ok=True)
        TARGET.write_text(expected)
