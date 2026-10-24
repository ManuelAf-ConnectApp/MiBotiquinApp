#!/usr/bin/env python3
"""Exercise SQL read from production Kotlin against exported Room schemas.

Uses Python's SQLite engine; does not replace Room/Kotlin or device tests.
Run: python3 scripts/verify_recovery_sql.py
"""
import json
from pathlib import Path
import re
import sqlite3
import unittest
import tempfile
from concurrent.futures import ThreadPoolExecutor

ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / 'data/src/commonMain/kotlin/com/connectapp/data/database/Database.kt'
DAO = ROOT / 'data/src/commonMain/kotlin/com/connectapp/data/database/dao/UserDao.kt'
SCHEMAS = ROOT / 'data/schemas/com.connectapp.data.database.AppDatabase'


def migration_sql(start, end):
    source = DATABASE.read_text()
    body = source.split(f'val MIGRATION_{start}_{end} =', 1)[1].split('\n}', 1)[0]
    calls = re.findall(r'connection\.execSQL\(\s*((?:"(?:[^"\\]|\\.)*"\s*\+?\s*)+)\)', body, re.S)
    return [''.join(json.loads(s) for s in re.findall(r'"(?:[^"\\]|\\.)*"', call)) for call in calls]


def query_for(method):
    source = DAO.read_text()
    pattern = r'@Query\("([^"\n]*)"\)\s+suspend fun ' + method + r'\('
    return re.search(pattern, source).group(1)


class RecoverySQLTest(unittest.TestCase):
    def database(self, version=7):
        db = sqlite3.connect(':memory:')
        self.addCleanup(db.close)
        db.execute('PRAGMA foreign_keys=ON')
        schema = json.loads((SCHEMAS / f'{version}.json').read_text())['database']
        for entity in schema['entities']:
            db.execute(entity['createSql'].replace('${TABLE_NAME}', entity['tableName']))
            for index in entity.get('indices', []):
                db.execute(index['createSql'].replace('${TABLE_NAME}', entity['tableName']))
        for row in [(17, 'One', 'User', 'one@example.com', 'hash-one', '', ''),
                    (28, 'Two', 'User', 'two@example.com', 'hash-two', '', '')]:
            db.execute('INSERT INTO users(id,firstName,lastName,email,password,createdAt,updatedAt) VALUES (?,?,?,?,?,?,?)', row)
        db.execute("INSERT INTO medications VALUES (9,'Test','17',2,0.5,'pills',10,NULL,NULL,0,9,0)")
        return db

    def test_supported_migrations_preserve_accounts_and_medication(self):
        for version in (5, 6, 7):
            with self.subTest(version=version):
                db = self.database(version)
                users = db.execute('SELECT * FROM users ORDER BY id').fetchall()
                medication = db.execute('SELECT * FROM medications').fetchall()
                for current in range(version, 8):
                    for sql in migration_sql(current, current + 1):
                        db.execute(sql)
                self.assertEqual(users, db.execute('SELECT id,firstName,lastName,email,password,createdAt,updatedAt FROM users ORDER BY id').fetchall())
                self.assertEqual(medication, [row[:len(medication[0])] for row in db.execute('SELECT * FROM medications').fetchall()])
                self.assertEqual([(None,), (None,)], db.execute('SELECT recoveryHash FROM users').fetchall())
                self.assertEqual([], db.execute('PRAGMA foreign_key_check').fetchall())

    def test_code_consumption_is_atomic_scoped_and_not_repeatable(self):
        db = self.database()
        for sql in migration_sql(7, 8):
            db.execute(sql)
        issue = query_for('setRecoveryHash')
        consume = query_for('consumeRecoveryCode')
        params = dict(id=17, expectedPassword='hash-one', recoveryHash='code-hash')
        self.assertEqual(1, db.execute(issue, params).rowcount)
        reset = dict(id=17, expectedRecoveryHash='code-hash', expectedPassword='hash-one', passwordHash='new-hash', updatedAt='now')
        self.assertEqual(0, db.execute(consume, {**reset, 'id': 28}).rowcount)
        self.assertEqual(0, db.execute(consume, {**reset, 'expectedRecoveryHash': 'wrong'}).rowcount)
        self.assertEqual(1, db.execute(consume, reset).rowcount)
        self.assertEqual(0, db.execute(consume, reset).rowcount)
        self.assertEqual(('new-hash', None), db.execute('SELECT password,recoveryHash FROM users WHERE id=17').fetchone())
        self.assertEqual(('hash-two',), db.execute('SELECT password FROM users WHERE id=28').fetchone())
        self.assertEqual(1, db.execute('SELECT COUNT(*) FROM medications').fetchone()[0])

    def test_rotating_code_or_password_invalidates_stale_updates(self):
        db = self.database()
        for sql in migration_sql(7, 8):
            db.execute(sql)
        issue = query_for('setRecoveryHash')
        db.execute(issue, dict(id=17, expectedPassword='hash-one', recoveryHash='first-code'))
        db.execute(issue, dict(id=17, expectedPassword='hash-one', recoveryHash='second-code'))
        reset = dict(id=17, expectedRecoveryHash='first-code', expectedPassword='hash-one', passwordHash='new-hash', updatedAt='now')
        self.assertEqual(0, db.execute(query_for('consumeRecoveryCode'), reset).rowcount)
        # Password rehash must preserve the newly issued code.
        upgrade = query_for('upgradePasswordHash')
        self.assertEqual(1, db.execute(upgrade, dict(id=17, expectedPassword='hash-one', passwordHash='upgraded-hash')).rowcount)
        self.assertEqual(('second-code',), db.execute('SELECT recoveryHash FROM users WHERE id=17').fetchone())
        self.assertEqual(0, db.execute(issue, dict(id=17, expectedPassword='hash-one', recoveryHash='stale-code')).rowcount)
        self.assertEqual(0, db.execute(query_for('consumeRecoveryCode'), {**reset, 'expectedRecoveryHash': 'second-code'}).rowcount)

    def test_medication_migration_keeps_legacy_amounts_without_rounding(self):
        db = self.database()
        for sql in migration_sql(7, 8):
            db.execute(sql)
        # Legacy inputs outside the new supported precision remain unchanged for review.
        db.execute("UPDATE medications SET quantity=0.3, quantityUnit='Pastillas'")
        before = db.execute('SELECT * FROM medications').fetchall()
        for sql in migration_sql(8, 9):
            db.execute(sql)
        after = db.execute('SELECT * FROM medications').fetchall()
        self.assertEqual(before, [row[:len(before[0])] for row in after])
        self.assertEqual([(None, 0, 0, '', '')], db.execute('SELECT quantityMinor,isIndefinite,isPaused,notes,pharmacologicalDose FROM medications').fetchall())
        self.assertEqual([], db.execute('PRAGMA foreign_key_check').fetchall())

    def test_supported_paths_reach_version_nine_without_losing_rows(self):
        for version in (5, 6, 7, 8):
            with self.subTest(version=version):
                db = self.database(version)
                for current in range(version, 9):
                    for sql in migration_sql(current, current + 1):
                        db.execute(sql)
                self.assertEqual(2, db.execute('SELECT COUNT(*) FROM users').fetchone()[0])
                self.assertEqual(1, db.execute('SELECT COUNT(*) FROM medications').fetchone()[0])
                db.execute("UPDATE medications SET quantityMinor=750,quantityUnit='tablet',isPaused=1,notes='Saved note'")
                self.assertEqual((750, 'tablet', 1, 'Saved note'), db.execute('SELECT quantityMinor,quantityUnit,isPaused,notes FROM medications').fetchone())
                self.assertEqual([], db.execute('PRAGMA foreign_key_check').fetchall())

    def dose_database(self):
        db = self.database()
        for current in range(7, 10):
            for sql in migration_sql(current, current + 1):
                db.execute(sql)
        db.execute("INSERT INTO dose_events VALUES ('9:1770000000000',9,'17',1770000000000,1769999400000,'PENDING',NULL,500,'tablet','Saved note','')")
        return db

    def dose_query(self, method):
        source = (ROOT / 'data/src/commonMain/kotlin/com/connectapp/data/database/dao/DoseEventDao.kt').read_text()
        return re.search(r'@Query\("([^"\n]*)"\)\s+suspend fun ' + method + r'\(', source).group(1)

    def test_dose_resolution_is_scoped_and_idempotent_and_deletion_cascades(self):
        db = self.dose_database()
        sql = self.dose_query('resolve')
        values = dict(id='9:1770000000000',owner='17',status='TAKEN',resolvedAt=1770000000001,quantityMinor=500,quantityUnit='tablet',notes='Saved note',pharmacologicalDose='')
        self.assertEqual(0, db.execute(sql, {**values, 'owner': '28'}).rowcount)
        self.assertEqual(1, db.execute(sql, values).rowcount)
        self.assertEqual(0, db.execute(sql, {**values, 'status': 'SKIPPED', 'resolvedAt': 999}).rowcount)
        self.assertEqual(('TAKEN',1770000000001,500), db.execute('SELECT status,resolvedAt,quantityMinor FROM dose_events').fetchone())
        db.execute("UPDATE medications SET name='Edited' WHERE id=9")
        self.assertEqual(1, db.execute('SELECT COUNT(*) FROM dose_events').fetchone()[0])
        db.execute('DELETE FROM medications WHERE id=9')
        self.assertEqual(0, db.execute('SELECT COUNT(*) FROM dose_events').fetchone()[0])
        self.assertEqual(2, db.execute('SELECT COUNT(*) FROM users').fetchone()[0])
        self.assertEqual([], db.execute('PRAGMA foreign_key_check').fetchall())

    def test_concurrent_dose_confirm_and_skip_have_exactly_one_winner(self):
        source = self.dose_database()
        source.commit()
        with tempfile.TemporaryDirectory(prefix='mibotiquin-dose-') as directory:
            path = Path(directory) / 'concurrent.db'
            target = sqlite3.connect(path)
            source.backup(target)
            target.close()
            sql = self.dose_query('resolve')
            def resolve(index):
                db = sqlite3.connect(path, timeout=10)
                try:
                    with db:
                        return db.execute(sql, dict(id='9:1770000000000',owner='17',status='TAKEN' if index % 2 == 0 else 'SKIPPED',resolvedAt=index,quantityMinor=500,quantityUnit='tablet',notes='',pharmacologicalDose='')).rowcount
                finally:
                    db.close()
            with ThreadPoolExecutor(max_workers=8) as workers:
                results = list(workers.map(resolve, range(16)))
            self.assertEqual(1, sum(results))

    def stock_query(self, method):
        source = (ROOT / 'data/src/commonMain/kotlin/com/connectapp/data/database/dao/StockDao.kt').read_text()
        return re.search(r'@Query\("([^"\n]*)"\)\s+suspend fun ' + method + r'\(', source).group(1)

    def stock_database(self):
        db = self.dose_database()
        users = db.execute('SELECT * FROM users').fetchall()
        medication = db.execute('SELECT * FROM medications').fetchall()
        doses = db.execute('SELECT * FROM dose_events').fetchall()
        for sql in migration_sql(10, 11):
            db.execute(sql)
        self.assertEqual(users, db.execute('SELECT * FROM users').fetchall())
        self.assertEqual(medication, [row[:len(medication[0])] for row in db.execute('SELECT * FROM medications').fetchall()])
        self.assertEqual(doses, db.execute('SELECT * FROM dose_events').fetchall())
        self.assertEqual(0, db.execute('SELECT COUNT(*) FROM stock_accounts').fetchone()[0])
        db.execute("INSERT INTO stock_accounts VALUES (9,'17','tablet',1000,1000,250,1,1)")
        db.execute("INSERT INTO stock_movements VALUES ('initial',9,'17','INITIAL',1000,1000,'tablet',1,'',NULL)")
        db.commit()
        return db

    def confirm_with_stock(self, db, owner='17'):
        with db:
            db.execute('BEGIN IMMEDIATE')
            event = db.execute(self.dose_query('get'), dict(id='9:1770000000000', owner=owner)).fetchone()
            if event is None or event[5] != 'PENDING':
                return 0
            account = db.execute(self.stock_query('account'), dict(medicationId=9, owner=owner)).fetchone()
            if account[4] < 500:
                return 0
            changed = db.execute(self.dose_query('resolve'), dict(id='9:1770000000000', owner=owner,status='TAKEN',resolvedAt=2,quantityMinor=500,quantityUnit='tablet',notes='',pharmacologicalDose='')).rowcount
            if changed:
                updated = db.execute(self.stock_query('updateBalance'), dict(medicationId=9,owner=owner,expectedBalance=account[4],balance=account[4]-500,updatedAt=2)).rowcount
                self.assertEqual(1, updated)
                db.execute("INSERT INTO stock_movements VALUES ('dose:9:1770000000000',9,'17','DOSE',-500,500,'tablet',2,'','9:1770000000000')")
            return changed

    def test_stock_consumption_and_dose_resolution_commit_once_together(self):
        db = self.stock_database()
        self.assertEqual(0, self.confirm_with_stock(db, owner='28'))
        self.assertEqual(1, self.confirm_with_stock(db))
        self.assertEqual(0, self.confirm_with_stock(db))
        self.assertEqual(500, db.execute('SELECT balanceMinor FROM stock_accounts').fetchone()[0])
        self.assertEqual(500, db.execute('SELECT SUM(deltaMinor) FROM stock_movements').fetchone()[0])
        self.assertEqual(1, db.execute("SELECT COUNT(*) FROM stock_movements WHERE kind='DOSE'").fetchone()[0])
        db.execute('DELETE FROM medications WHERE id=9')
        db.commit()
        for table in ('dose_events','stock_accounts','stock_movements'):
            self.assertEqual(0, db.execute(f'SELECT COUNT(*) FROM {table}').fetchone()[0])
        self.assertEqual([], db.execute('PRAGMA foreign_key_check').fetchall())

    def test_stock_write_failure_rolls_back_both_balance_and_dose(self):
        db = self.stock_database()
        db.execute("CREATE TRIGGER reject_consumption BEFORE INSERT ON stock_movements WHEN NEW.kind='DOSE' BEGIN SELECT RAISE(ABORT,'simulated write failure'); END")
        db.commit()
        with self.assertRaises(sqlite3.IntegrityError):
            self.confirm_with_stock(db)
        self.assertEqual(('PENDING',None), db.execute('SELECT status,resolvedAt FROM dose_events').fetchone())
        self.assertEqual(1000, db.execute('SELECT balanceMinor FROM stock_accounts').fetchone()[0])
        self.assertEqual(1, db.execute('SELECT COUNT(*) FROM stock_movements').fetchone()[0])

    def test_concurrent_stock_consumptions_debit_exactly_once(self):
        source = self.stock_database()
        with tempfile.TemporaryDirectory(prefix='mibotiquin-stock-') as directory:
            path = Path(directory) / 'stock.db'
            target = sqlite3.connect(path)
            source.backup(target)
            target.close()
            def confirm(_):
                db = sqlite3.connect(path, timeout=10)
                db.execute('PRAGMA foreign_keys=ON')
                try:
                    return self.confirm_with_stock(db)
                finally:
                    db.close()
            with ThreadPoolExecutor(max_workers=8) as workers:
                self.assertEqual(1, sum(workers.map(confirm, range(16))))
            db = sqlite3.connect(path)
            try:
                self.assertEqual(500, db.execute('SELECT balanceMinor FROM stock_accounts').fetchone()[0])
                self.assertEqual(1, db.execute("SELECT COUNT(*) FROM stock_movements WHERE kind='DOSE'").fetchone()[0])
            finally:
                db.close()

    def test_unsupported_versions_have_no_destructive_fallback(self):
        self.assertNotIn('.fallbackToDestructiveMigration', DATABASE.read_text())


if __name__ == '__main__':
    unittest.main(verbosity=2)
