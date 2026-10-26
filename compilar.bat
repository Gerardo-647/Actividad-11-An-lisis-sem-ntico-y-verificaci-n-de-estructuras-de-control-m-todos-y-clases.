@echo off
cd /d "%~dp0"
chcp 65001 > nul
javac -encoding UTF-8 -cp ".;java-cup-11b-runtime.jar" Lexer.java sym.java Parser.java Main.java Expr.java ManejadorTipos.java Nodo.java Argumentos.java MarcaAmbito.java Simbolo.java ManejadorAmbitos.java TablaSimbolos.java ManejadorClases.java ManejadorMetodos.java
if errorlevel 1 exit /b 1
echo Compilacion terminada.
