#!/usr/bin/env python3
"""Compile real domain sources and run selected JVM tests with cached dependencies.

No dependencies are downloaded. This does not compile Compose, run KSP or validate
Android/iOS artifacts. The normal Gradle checks remain required before release.
"""
from pathlib import Path
import subprocess
import os
from tempfile import TemporaryDirectory
root=Path(__file__).resolve().parents[1]
os.chdir(root)
cache=Path(os.environ.get('GRADLE_USER_HOME', str(Path.home()/'.gradle'))) / 'caches/modules-2/files-2.1'
def jar(group,artifact,version):
 candidates = list((cache/group/artifact/version).glob(f'*/{artifact}-{version}.jar'))
 if not candidates:
  raise SystemExit(f"Missing cached dependency: {group}:{artifact}:{version}. Run the normal Gradle build first.")
 return str(candidates[0])
std=jar('org.jetbrains.kotlin','kotlin-stdlib','2.4.0')
annotations=jar('org.jetbrains','annotations','23.0.0')
compiler=[jar('org.jetbrains.kotlin','kotlin-compiler-embeddable','2.4.0'), std, annotations,
 jar('org.jetbrains.kotlin','kotlin-script-runtime','2.4.0'),
 jar('org.jetbrains.kotlin','kotlin-reflect','2.2.21'),
 jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm','1.10.2')]
deps=[std,annotations,jar('org.jetbrains.kotlinx','kotlinx-datetime-jvm','0.8.0'),
 jar('org.jetbrains.kotlinx','kotlinx-serialization-core-jvm','1.11.0'),
 jar('org.jetbrains.kotlinx','kotlinx-serialization-json-jvm','1.11.0'),
 jar('org.jetbrains.kotlin','kotlin-test','2.4.0'),jar('org.jetbrains.kotlin','kotlin-test-junit','2.4.0'),
 jar('androidx.room','room-common-jvm','2.8.4'),
 jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm','1.10.2'),
 jar('junit','junit','4.13.2'),jar('org.hamcrest','hamcrest-core','1.3')]
sources=[
 'domain/src/commonMain/kotlin/com/connectapp/domain/model/DoseQuantity.kt',
 'domain/src/commonMain/kotlin/com/connectapp/domain/model/Medication.kt',
 'domain/src/commonMain/kotlin/com/connectapp/domain/validator/MedicationRules.kt',
 'domain/src/commonMain/kotlin/com/connectapp/domain/reminder/DoseSchedule.kt',
 'domain/src/commonTest/kotlin/com/connectapp/domain/model/DoseQuantityTest.kt',
 'domain/src/commonTest/kotlin/com/connectapp/domain/validator/MedicationRulesTest.kt',
 'domain/src/commonTest/kotlin/com/connectapp/domain/reminder/DoseScheduleTest.kt']
sources += [str(p) for p in (root/'domain/src/commonMain/kotlin').rglob('*.kt') if p.name != 'Platform.kt' and str(p.relative_to(root)) not in sources]
sources += [
 'data/src/commonMain/kotlin/com/connectapp/data/database/entity/UserEntity.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/database/entity/MedicationEntity.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/model/MedicationDto.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/mapper/MedicationMapper.kt',
 'data/src/commonTest/kotlin/com/connectapp/data/mapper/MedicationPersistenceTest.kt']
sources += [
 'data/src/commonMain/kotlin/com/connectapp/data/database/entity/DoseEventEntity.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/database/dao/DoseEventDao.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/database/dao/MedicationDao.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/datasource/DoseLocalDataSource.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/datasource/AuthLocalDataSource.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/security/SecureStorage.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/repository/DoseRepositoryImpl.kt',
 'data/src/commonTest/kotlin/com/connectapp/data/repository/DoseRepositoryTest.kt',
 'domain/src/commonTest/kotlin/com/connectapp/domain/notification/ReminderNavigationTest.kt']
sources += [
 'data/src/commonMain/kotlin/com/connectapp/data/database/entity/StockAccountEntity.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/database/entity/StockMovementEntity.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/database/dao/StockDao.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/repository/StockRepositoryImpl.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/datasource/MedicationDataSource.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/datasource/impl/MedicationDataSourceImpl.kt',
 'data/src/commonTest/kotlin/com/connectapp/data/repository/FakeStockDao.kt',
 'data/src/commonTest/kotlin/com/connectapp/data/repository/StockRepositoryTest.kt',
 'domain/src/commonTest/kotlin/com/connectapp/domain/stock/StockEstimatorTest.kt']
sources += [
 'data/src/commonMain/kotlin/com/connectapp/data/datasource/CatalogueHttpClient.kt',
 'data/src/commonMain/kotlin/com/connectapp/data/repository/CatalogueRepositoryImpl.kt',
 'data/src/androidMain/kotlin/com/connectapp/data/catalogue/AndroidCatalogueHttpClient.kt',
 'data/src/commonTest/kotlin/com/connectapp/data/repository/CatalogueRepositoryTest.kt',
 'domain/src/commonTest/kotlin/com/connectapp/domain/catalogue/SpanishMedicationCodeTest.kt',
 'domain/src/commonTest/kotlin/com/connectapp/domain/catalogue/GtinTest.kt']
temporary_output = TemporaryDirectory(prefix='mibotiquin-domain-')
out = temporary_output.name
subprocess.run(['java','-cp',':'.join(compiler),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-classpath',':'.join(deps),'-d',out,*sources],check=True)
subprocess.run(['java','-cp',':'.join([out,*deps]),'org.junit.runner.JUnitCore',
 'com.connectapp.domain.model.DoseQuantityTest','com.connectapp.domain.validator.MedicationRulesTest','com.connectapp.domain.reminder.DoseScheduleTest','com.connectapp.data.mapper.MedicationPersistenceTest','com.connectapp.data.repository.DoseRepositoryTest','com.connectapp.domain.notification.ReminderNavigationTest','com.connectapp.data.repository.StockRepositoryTest','com.connectapp.domain.stock.StockEstimatorTest','com.connectapp.data.repository.CatalogueRepositoryTest','com.connectapp.domain.catalogue.SpanishMedicationCodeTest','com.connectapp.domain.catalogue.GtinTest'],check=True)
