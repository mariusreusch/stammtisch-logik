package com.stammtisch.mcp;

import com.stammtisch.mcp.service.McpContextProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpApplication
//        implements CommandLineRunner
{
    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider stammTischTools(McpContextProvider McpContextProvider) {
        return MethodToolCallbackProvider.builder().toolObjects(McpContextProvider).build();
    }

   /* @Autowired
    McpContextProvider mcpContextProvider;

    @Override
    public void run(String... args) throws Exception {
        mcpContextProvider.getAllStammtischGuidelines();
    }*/
}
