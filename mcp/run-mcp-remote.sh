#!/bin/bash

# MCP Remote Server Runner for Linux/macOS
# Usage: ./run-mcp-remote.sh [MCP_SERVER_URL]
# Default URL: http://localhost:8080/sse

set -e

echo "========================================="
echo "MCP Remote Server Runner"
echo "========================================="

# Get the MCP server URL from parameter or use default
MCP_URL="${1:-http://localhost:8080/sse}"

echo "Connecting to MCP server: $MCP_URL"
echo ""

# Get the project root directory (directory of this script)
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

# Set Node.js paths
NODE_DIR="$PROJECT_ROOT/build/nodejs/node-v20.11.0-linux-x64"
if [[ "$OSTYPE" == "darwin"* ]]; then
    NODE_DIR="$PROJECT_ROOT/build/nodejs/node-v20.11.0-darwin-x64"
fi
NPM_DIR="$PROJECT_ROOT/build/npm"

# Check if Node.js is installed via Gradle
if [ ! -f "$NODE_DIR/bin/node" ]; then
    echo "Node.js not found. Setting up Node.js via Gradle..."
    cd "$PROJECT_ROOT"
    ./gradlew npmSetup
    if [ $? -ne 0 ]; then
        echo "Failed to set up Node.js"
        echo "Please run: ./gradlew npmSetup"
        exit 1
    fi
fi

# Check if mcp-remote is installed
if [ ! -d "$NODE_DIR/lib/node_modules/mcp-remote" ] && [ ! -d "$NODE_DIR/node_modules/mcp-remote" ]; then
    echo "Installing mcp-remote package..."
    cd "$PROJECT_ROOT"
    ./gradlew installMcpRemote
    if [ $? -ne 0 ]; then
        echo "Failed to install mcp-remote"
        echo "Please run: ./gradlew installMcpRemote"
        exit 1
    fi
fi

# Set up environment variables
export NODE_HOME="$NODE_DIR"
export PATH="$NODE_DIR/bin:$NPM_DIR/bin:$PATH"

echo "Starting MCP remote connection..."
echo "Press Ctrl+C to stop"
echo ""

# Run mcp-remote with the provided URL
npx -y mcp-remote "$MCP_URL"
