@echo off
rem GitHub Management CLI - Windows launcher
setlocal
pushd "%~dp0"
java -Dghcp-mgmt.home="%CD%" -jar "target\ghcp-mgmt-1.0.0.jar" %*
endlocal
