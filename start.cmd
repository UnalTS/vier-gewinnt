@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac --release 17 -encoding UTF-8 -d out src\Game.java src\VierGewinnt.java
if errorlevel 1 exit /b 1
java -cp out VierGewinnt
