@echo off
rem ArchForge developer CLI launcher for Windows (counterpart of the ./archforge bash script).
setlocal

set "ROOT=%~dp0"
set "JAR=%ROOT%archforge-cli\build\libs\archforge-cli.jar"

if exist "%JAR%" goto run

echo archforge-cli.jar not found, building...
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
