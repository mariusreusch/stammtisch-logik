package com.stammtisch.mcp.service;

import io.modelcontextprotocol.sdk.context.ContextProvider;
import io.modelcontextprotocol.sdk.context.FileContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class McpContextProvider implements ContextProvider {

    private final GitHubService gitHubService;

    @Override
    public Map<String, Object> getContext(String query) {
        Map<String, Object> context = new HashMap<>();
        List<Map<String, String>> documents = new ArrayList<>();

        // Find relevant documents from the GitHub repository
        List<String> markdownFiles = gitHubService.searchAllMarkdownFiles();

        for (String path : markdownFiles) {
            String content = gitHubService.getDocumentContent(path);
            Map<String, String> document = new HashMap<>();
            document.put("path", path);
            document.put("content", content);
            documents.add(document);
        }

        context.put("project", "stammtisch-logik");
        context.put("documents", documents);

        // Add architecture principles
        context.put("architecture", "Onion Architecture");
        context.put("design_patterns", "Domain-Driven Design");

        return context;
    }
}
