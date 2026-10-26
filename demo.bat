@echo off
cd /d "%~dp0"
call compilar.bat
if errorlevel 1 exit /b 1
java -cp ".;java-cup-11b-runtime.jar" Main prueba7_control_valido.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main prueba9_anidamiento.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main prueba8_control_invalido.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main prueba10_recuperacion.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main prueba11_combinado.txt
pause
