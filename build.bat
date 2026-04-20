@echo off
echo ============================================
echo  Smart Campus API - Build Script
echo ============================================
echo.

REM Try to find mvn in PATH first
where mvn >nul 2>&1
if %errorlevel% == 0 (
    echo Found Maven in PATH. Building...
    mvn clean package -DskipTests
    goto :done
)

REM Try common install locations
if exist "C:\Program Files\Apache\Maven\bin\mvn.cmd" (
    set MVN="C:\Program Files\Apache\Maven\bin\mvn.cmd"
    goto :build
)
if exist "C:\tools\maven\bin\mvn.cmd" (
    set MVN="C:\tools\maven\bin\mvn.cmd"
    goto :build
)
if defined M2_HOME (
    set MVN="%M2_HOME%\bin\mvn.cmd"
    goto :build
)
if defined MAVEN_HOME (
    set MVN="%MAVEN_HOME%\bin\mvn.cmd"
    goto :build
)

echo ERROR: Maven not found! Please install Maven and add it to PATH.
echo Download from: https://maven.apache.org/download.cgi
echo.
echo Alternatively, set the MAVEN_HOME environment variable.
pause
exit /b 1

:build
echo Using Maven at %MVN%
%MVN% clean package -DskipTests

:done
if %errorlevel% == 0 (
    echo.
    echo ============================================
    echo  BUILD SUCCESSFUL!
    echo  JAR: target\smart-campus-api-1.0.0.jar
    echo ============================================
) else (
    echo.
    echo ============================================
    echo  BUILD FAILED! Check output above.
    echo ============================================
    pause
    exit /b 1
)
pause
