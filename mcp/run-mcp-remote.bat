@echo off
setlocal enabledelayedexpansion

REM MCP Remote Server Runner for Windows
REM Usage: run-mcp-remote.bat [MCP_SERVER_URL]
REM Default URL: http://localhost:8080/sse

echo =========================================
echo MCP Remote Server Runner
echo =========================================

REM Get the MCP server URL from parameter or use default
set MCP_URL=%~1
if "%MCP_URL%"=="" set MCP_URL=http://localhost:8080/sse

echo Connecting to MCP server: %MCP_URL%
echo.

REM Get the project root directory (current directory)
set PROJECT_ROOT=%~dp0
set PROJECT_ROOT=%PROJECT_ROOT:~0,-1%

REM Set Node.js paths
set NODE_DIR=%PROJECT_ROOT%\build\nodejs\node-v20.11.0-win-x64
set NPM_DIR=%PROJECT_ROOT%\build\npm

REM Check if Node.js is installed via Gradle
if not exist "%NODE_DIR%\node.exe" (
    echo Node.js not found. Setting up Node.js via Gradle...
    call "%PROJECT_ROOT%\gradlew.bat" npmSetup
    if errorlevel 1 (
        echo Failed to set up Node.js
        echo Please run: gradlew.bat npmSetup
        exit /b 1
    )
)

REM Check if mcp-remote is installed
if not exist "%NODE_DIR%\node_modules\mcp-remote" (
    echo Installing mcp-remote package...
    call "%PROJECT_ROOT%\gradlew.bat" installMcpRemote
    if errorlevel 1 (
        echo Failed to install mcp-remote
        echo Please run: gradlew.bat installMcpRemote
        exit /b 1
    )
)

REM Set up environment variables
set NODE_HOME=%NODE_DIR%
set PATH=%NODE_DIR%;%NODE_DIR%\node_modules\.bin;%NPM_DIR%;%PATH%

echo Starting MCP remote connection...
echo Press Ctrl+C to stop
echo.

REM Run mcp-remote with the provided URL
"%NODE_DIR%\npx.cmd" -y mcp-remote "%MCP_URL%"

endlocal
