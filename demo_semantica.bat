@echo off
cd /d "%~dp0"
chcp 65001 > nul
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_valido_calculadora.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_invalido_calculadora_errores.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_invalido_control.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_invalido_metodos.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_invalido_retorno_caminos.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_valido_clases.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_invalido_clases.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_invalido_ambitos.txt
pause
java -cp ".;java-cup-11b-runtime.jar" Main pruebas_semantica\sem_invalido_multiple.txt
