@echo off
echo.

echo Criando pasta bin...
mkdir bin 2>nul

echo Compilando programa...
javac -d bin Main.java

echo Rodando programa...
timeout /t 3
cls

java -cp bin Main -u "root"
pause
