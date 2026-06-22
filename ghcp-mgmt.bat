@echo off
rem GitHub Management CLI - Windows launcher
setlocal
pushd "%~dp0"
if exist "target\ghcp-mgmt-1.0.0.jar" (
    set "JAR=target\ghcp-mgmt-1.0.0.jar"
) else (
    set "JAR=ghcp-mgmt-1.0.0.jar"
)
java -Dghcp-mgmt.home="%CD%" -jar "%JAR%" %*
endlocal
