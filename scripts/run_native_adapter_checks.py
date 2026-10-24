#!/usr/bin/env python3
"""Compile isolated iOS camera/HTTP adapters using a preinstalled Kotlin/Native toolchain.

No app linking, Compose generation, camera execution or network integration is performed.
Dependencies must already exist in the normal Gradle/Konan caches.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
import os
import subprocess

root = Path(__file__).resolve().parents[1]
home = Path.home()
compiler = home / '.konan/kotlin-native-prebuilt-macos-aarch64-2.4.0/bin/konanc'
cache = Path(os.environ.get('GRADLE_USER_HOME', str(home / '.gradle'))) / 'caches/modules-2/files-2.1/org.jetbrains.kotlinx'
coroutines = list((cache / 'kotlinx-coroutines-core-iossimulatorarm64/1.11.0').rglob('*.klib'))
atomic = list((cache / 'atomicfu-iossimulatorarm64/0.32.1').rglob('*.klib'))
if not compiler.is_file() or not coroutines or not atomic:
    raise SystemExit('Missing cached Kotlin/Native 2.4.0 or native dependencies. Run the normal Gradle build first.')
with TemporaryDirectory(prefix='mibotiquin-native-') as work:
    temporary = Path(work)
    (temporary / 'dependencies').symlink_to(home / '.konan/dependencies', target_is_directory=True)
    env = {**os.environ, 'KONAN_DATA_DIR': str(temporary)}
    base = [str(compiler), '-produce', 'library', '-target', 'ios_simulator_arm64']
    subprocess.run(base + ['-o', str(temporary / 'camera'),
        'presentation/src/commonMain/kotlin/com/connectapp/presentation/scan/CodeScanner.kt',
        'presentation/src/iosMain/kotlin/com/connectapp/presentation/scan/IOSCodeScanner.kt'], cwd=root, env=env, check=True)
    libraries = [arg for path in coroutines + atomic for arg in ['-library', str(path)]]
    subprocess.run(base + ['-o', str(temporary / 'http')] + libraries + [
        'data/src/commonMain/kotlin/com/connectapp/data/datasource/CatalogueHttpClient.kt',
        'data/src/iosMain/kotlin/com/connectapp/data/catalogue/IOSCatalogueHttpClient.kt'], cwd=root, env=env, check=True)
print('Isolated iOS camera and HTTP adapters compiled; full application/native execution still required.')
