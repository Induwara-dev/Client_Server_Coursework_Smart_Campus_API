@echo off
echo ============================================
echo  Smart Campus API - Run Server
echo ============================================
echo.
if not exist "target\smart-campus-api-1.0.0.jar" (
    echo JAR not found. Running build first...
    call build.bat
    if %errorlevel% neq 0 exit /b 1
)
echo Starting server at http://localhost:8080/api/v1
echo Press ENTER in the server window to stop.
echo.
java -jar target\smart-campus-api-1.0.0.jar
