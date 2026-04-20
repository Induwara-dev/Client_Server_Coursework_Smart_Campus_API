@REM Maven Wrapper Script for Windows
@REM Downloads Maven automatically if not present

@echo off
set MAVEN_WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
set MAVEN_PROJECTBASEDIR=%~dp0

if exist "%MAVEN_WRAPPER_JAR%" goto validateMavenWrapper

echo Downloading maven-wrapper.jar...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%MAVEN_WRAPPER_JAR%')}"

:validateMavenWrapper
java -jar "%MAVEN_WRAPPER_JAR%" %*
