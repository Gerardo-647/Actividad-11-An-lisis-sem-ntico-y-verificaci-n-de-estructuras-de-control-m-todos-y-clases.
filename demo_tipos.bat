@echo off
cd /d "%~dp0"
chcp 65001 > nul
call compilar.bat
if errorlevel 1 exit /b 1
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_tipos\tipos_valido_basico.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_tipos\tipos_invalido_basico.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_tipos\tipos_valido_promocion.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_tipos\tipos_invalido_operadores.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_tipos\tipos_invalido_condiciones.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_tipos\tipos_invalido_constantes.txt
pause
