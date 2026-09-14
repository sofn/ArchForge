@echo off
rem ArchForge developer CLI launcher for Windows (counterpart of the ./archforge bash script).
setlocal

set "ROOT=%~dp0"
set "JAR=%ROOT%archforge-cli\build\libs\archforge-cli.jar"

rem ArchForge requires JDK 25+; fail readably instead of UnsupportedClassVersionError.
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVAVER=%%~v"
for /f "delims=." %%m in ("%JAVAVER%") do set "JAVAMAJOR=%%m"
if not defined JAVAMAJOR set "JAVAMAJOR=0"
if %JAVAMAJOR% LSS 25 (
  echo ERROR: ArchForge requires JDK 25+ ^(found: %JAVAVER%^). Fix JAVA_HOME or PATH. 1>&2
  exit /b 1
)

rem Rebuild when the jar is missing or any cli source is newer than the jar.
powershell -NoProfile -Command "if (!(Test-Path '%JAR%')) { exit 0 }; $jarTime=(Get-Item '%JAR%').LastWriteTime; $newer = Get-ChildItem '%ROOT%archforge-cli\src' -Recurse -File | Where-Object { $_.LastWriteTime -gt $jarTime } | Select-Object -First 1; if ($newer -or (Get-Item '%ROOT%archforge-cli\build.gradle.kts').LastWriteTime -gt $jarTime) { exit 0 }; exit 1" >nul 2>&1
if %errorlevel% neq 0 goto run

echo archforge-cli sources changed - rebuilding jar...
pushd "%ROOT%"
call gradlew.bat :archforge-cli:shadowJar -x test
if errorlevel 1 (
  echo build failed
  popd
  exit /b 1
)
popd

:run
java --enable-preview -jar "%JAR%" %*
