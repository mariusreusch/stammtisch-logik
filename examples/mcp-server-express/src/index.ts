import { CallToolRequestSchema, ListToolsRequestSchema, } from "@modelcontextprotocol/sdk/types.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {Server} from "@modelcontextprotocol/sdk/server/index.js";

const logger = require('pino')({
    level: process.env.LOG_LEVEL || 'debug'
});
// The server instance and tools exposed to Claude
const server = new Server({
    name: "time-server",
    version: "0.0.1",
}, {
    capabilities: {
        tools: {},
    },
});

server.setRequestHandler(ListToolsRequestSchema, async () => {
    return {
        tools: [
            {
                name: "get_time_and_date",
                description: "Returns the current time and date in ISO format",
                inputSchema: {
                },
            },
        ],
    };
});
server.setRequestHandler(CallToolRequestSchema, async (request) => {
    const {name, arguments: args} = request.params;
    if (!args) {
        throw new Error(`No arguments provided for tool: ${name}`);
    }
    switch (name) {
        case "get_time_and_date":
            return {
                content: [{
                    type: "text",
                    text: JSON.stringify({time: new Date().toISOString()}, null, 2)
                }]
            };
        default:
            throw new Error(`Unknown tool: ${name}`);
    }
});

async function main() {
    const transport = new StdioServerTransport();
    await server.connect(transport);
    console.error("Knowledge Graph MCP Server running on stdio");
}

main().catch((error) => {
    console.error("Fatal error in main():", error);
    process.exit(1);
});
