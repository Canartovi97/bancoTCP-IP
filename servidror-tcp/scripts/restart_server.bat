@echo off

:: Ruta al archivo .jar del servidor headless
set SERVER_JAR_PATH=C:\Users\camil\Documents\sistemas_distribuidos\Servidor_Banco\servidror-tcp\target\servidror-tcp-1.0-SNAPSHOT.jar

:: Nombre del proceso del servidor
set SERVER_PROCESS_NAME=servidror-tcp-1.0-SNAPSHOT-headless.jar

:: Iniciar el servidor en modo headless
echo Iniciando el servidor...
start /B java -jar "%SERVER_JAR_PATH%" > C:\Users\camil\Documents\sistemas_distribuidos\Servidor_Banco\servidror-tcp\target\server.log 2>&1

echo Servidor reiniciado.