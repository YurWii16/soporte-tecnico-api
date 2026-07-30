@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot
set PATH=C:\Tools\maven\apache-maven-3.9.6\bin;%JAVA_HOME%\bin;%PATH%
cd /d %~dp0
echo Iniciando API REST en http://localhost:8080/api/swagger-ui/index.html
mvn spring-boot:run
