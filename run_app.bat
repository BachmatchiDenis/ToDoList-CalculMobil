@echo off
echo ========================================
echo    ToDo List - Build and Run Script
echo ========================================
echo.

cd /d C:\Users\Denis\AndroidStudioProjects\ToDoList

REM Define SDK paths
set SDK_ROOT=C:\Users\Denis\AppData\Local\Android\Sdk
set ADB=%SDK_ROOT%\platform-tools\adb.exe
set EMULATOR_EXE=%SDK_ROOT%\emulator\emulator.exe

echo [1/4] Opresc emulatorul existent...
"%ADB%" emu kill 2>nul
timeout /t 2 >nul

echo [2/4] Pornesc emulatorul (Medium Phone API)...
REM Găsește numele emulatorului
for /f "tokens=*" %%i in ('"%EMULATOR_EXE%" -list-avds') do (
    set EMULATOR_NAME=%%i
    goto :found_emulator
)
:found_emulator
echo     Emulator: %EMULATOR_NAME%
start "" "%EMULATOR_EXE%" -avd %EMULATOR_NAME% -wipe-data

echo [3/4] Aștept să pornească emulatorul...
:wait_for_device
"%ADB%" wait-for-device
timeout /t 5 >nul
"%ADB%" shell getprop sys.boot_completed | find "1" >nul
if errorlevel 1 goto wait_for_device
echo     Emulatorul este gata!

echo [4/4] Compilez și instalez aplicația...
call gradlew.bat installDebug

if errorlevel 1 (
    echo.
    echo EROARE: Compilarea a esuat!
    pause
    exit /b 1
)

echo.
echo [5/5] Lansez aplicația...
"%ADB%" shell am start -n com.example.todolist/.ui.LoginActivity

echo.
echo ========================================
echo    Aplicația ToDo List rulează!
echo ========================================
pause

