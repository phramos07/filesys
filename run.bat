@echo off
echo.

echo Compilando programa...
javac Main.java

echo Rodando programa...
timeout 3
cls

java Main -u "root"
pause