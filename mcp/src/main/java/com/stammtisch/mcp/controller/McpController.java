package com.stammtisch.mcp.controller;

import com.stammtisch.mcp.service.GitHubService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
@Slf4j
public class McpController {

    private final GitHubService gitHubService;

    @PostMapping("/tools/search-documents")
    public ResponseEntity<Map<String, Object>> searchDocuments(
        @RequestBody Map<String, Object> request
    ) {
        log.info("Received MCP tool call for document search: {}", request);

        String query = (String) request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Query parameter is required"));
        }

        try {
            // Search for markdown files that contain the query
            List<String> allFiles = gitHubService.searchAllMarkdownFiles();
            List<Map<String, Object>> results = allFiles
                .stream()
                .filter(path -> {
                    String content = gitHubService.getDocumentContent(path);
                    return content.toLowerCase().contains(query.toLowerCase());
                })
                .map(path -> {
                    String content = gitHubService.getDocumentContent(path);
                    return Map.<String, Object>of(
                        "path",
                        path,
                        "title",
                        path.substring(path.lastIndexOf('/') + 1),
                        "content",
                        content.length() > 500
                            ? content.substring(0, 500) + "..."
                            : content,
                        "url",
                        "https://github.com/mariusreusch/stammtisch-logik/blob/main/" +
                        path
                    );
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                Map.of(
                    "results",
                    results,
                    "query",
                    query,
                    "totalFound",
                    results.size()
                )
            );
        } catch (Exception e) {
            log.error("Error searching documents", e);
            return ResponseEntity.internalServerError()
                .body(
                    Map.of(
                        "error",
                        "Failed to search documents: " + e.getMessage()
                    )
                );
        }
    }

    @PostMapping("/tools/get-document")
    public ResponseEntity<Map<String, Object>> getDocument(
        @RequestBody Map<String, Object> request
    ) {
        log.info("Received MCP tool call for document retrieval: {}", request);

        String path = (String) request.get("path");
        if (path == null || path.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Path parameter is required"));
        }

        try {
            String content = gitHubService.getDocumentContent(path);

            return ResponseEntity.ok(
                Map.of(
                    "path",
                    path,
                    "content",
                    content,
                    "url",
                    "https://github.com/mariusreusch/stammtisch-logik/blob/main/" +
                    path
                )
            );
        } catch (Exception e) {
            log.error("Error retrieving document: {}", path, e);
            return ResponseEntity.internalServerError()
                .body(
                    Map.of(
                        "error",
                        "Failed to retrieve document: " + e.getMessage()
                    )
                );
        }
    }

    @PostMapping("/tools/list-documents")
    public ResponseEntity<Map<String, Object>> listDocuments(
        @RequestBody(required = false) Map<String, Object> request
    ) {
        log.info("Received MCP tool call for listing documents");

        try {
            List<String> markdownFiles = gitHubService.searchAllMarkdownFiles();
            List<String> guidelineFiles =
                gitHubService.listGuidelineDocuments();

            return ResponseEntity.ok(
                Map.of(
                    "allMarkdownFiles",
                    markdownFiles,
                    "guidelineFiles",
                    guidelineFiles,
                    "totalCount",
                    markdownFiles.size()
                )
            );
        } catch (Exception e) {
            log.error("Error listing documents", e);
            return ResponseEntity.internalServerError()
                .body(
                    Map.of(
                        "error",
                        "Failed to list documents: " + e.getMessage()
                    )
                );
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(
            Map.of(
                "status",
                "running",
                "service",
                "MCP Server - Stammtisch Logik Documentation",
                "version",
                "1.0.0",
                "repository",
                "mariusreusch/stammtisch-logik",
                "availableTools",
                List.of("search-documents", "get-document", "list-documents")
            )
        );
    }

    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getAvailableTools() {
        return ResponseEntity.ok(
            Map.of(
                "tools",
                List.of(
                    Map.of(
                        "name",
                        "search-documents",
                        "description",
                        "Search through all markdown documents for content matching a query",
                        "inputSchema",
                        Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                "query",
                                Map.of(
                                    "type",
                                    "string",
                                    "description",
                                    "The search query to look for in documents"
                                )
                            ),
                            "required",
                            List.of("query")
                        )
                    ),
                    Map.of(
                        "name",
                        "get-document",
                        "description",
                        "Retrieve the full content of a specific document by path",
                        "inputSchema",
                        Map.of(
                            "type",
                            "object",
                            "properties",
                            Map.of(
                                "path",
                                Map.of(
                                    "type",
                                    "string",
                                    "description",
                                    "The path to the document to retrieve"
                                )
                            ),
                            "required",
                            List.of("path")
                        )
                    ),
                    Map.of(
                        "name",
                        "list-documents",
                        "description",
                        "List all available markdown documents in the repository",
                        "inputSchema",
                        Map.of("type", "object", "properties", Map.of())
                    )
                )
            )
        );
    }
}
