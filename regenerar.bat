@echo off
cd /d "%~dp0"
chcp 65001 > nul
java -jar java-cup-11b.jar -parser Parser -symbols sym Parser.cup
if errorlevel 1 exit /b 1
java -jar jflex-full.jar Lexer.jflex
if errorlevel 1 exit /b 1
call compilar.bat
