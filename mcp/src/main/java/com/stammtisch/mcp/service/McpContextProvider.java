package com.stammtisch.mcp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpContextProvider {

    private final GitHubService gitHubService;

    /**
     * Get context information for MCP operations
     * @param query The search query or context request
     * @return A map containing context information
     */
    public Map<String, Object> getContext(String query) {
        log.debug("Getting context for query: {}", query);

        Map<String, Object> context = new HashMap<>();
        List<Map<String, String>> documents = new ArrayList<>();

        try {
            // Find relevant documents from the GitHub repository
            List<String> markdownFiles = gitHubService.searchAllMarkdownFiles();
            log.info(
                "Found {} markdown files for context",
                markdownFiles.size()
            );

            for (String path : markdownFiles) {
                try {
                    String content = gitHubService.getDocumentContent(path);
                    if (content != null && !content.startsWith("Error:")) {
                        Map<String, String> document = new HashMap<>();
                        document.put("path", path);
                        document.put("title", extractTitle(path));
                        document.put("content", content);
                        documents.add(document);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get content for path: {}", path, e);
                }
            }

            // Add project metadata
            context.put("project", "stammtisch-logik");
            context.put("repository", "mariusreusch/stammtisch-logik");
            context.put("documents", documents);
            context.put("totalDocuments", documents.size());

            // Add architecture and design information
            context.put("architecture", "Onion Architecture");
            context.put("designPatterns", "Domain-Driven Design");
            context.put("framework", "Spring Boot");
            context.put("language", "Java");

            log.info("Context prepared with {} documents", documents.size());
        } catch (Exception e) {
            log.error("Error preparing context", e);
            context.put(
                "error",
                "Failed to prepare context: " + e.getMessage()
            );
        }

        return context;
    }

    /**
     * Get context specifically for guideline documents
     * @return A map containing guideline context
     */
    public Map<String, Object> getGuidelineContext() {
        log.debug("Getting guideline context");

        Map<String, Object> context = new HashMap<>();
        List<Map<String, String>> guidelines = new ArrayList<>();

        try {
            List<String> guidelineFiles =
                gitHubService.listGuidelineDocuments();
            log.info("Found {} guideline files", guidelineFiles.size());

            for (String fileName : guidelineFiles) {
                try {
                    String path = "doc/" + fileName;
                    String content = gitHubService.getDocumentContent(path);
                    if (content != null && !content.startsWith("Error:")) {
                        Map<String, String> guideline = new HashMap<>();
                        guideline.put("fileName", fileName);
                        guideline.put("path", path);
                        guideline.put("title", extractTitle(fileName));
                        guideline.put("content", content);
                        guidelines.add(guideline);
                    }
                } catch (Exception e) {
                    log.warn(
                        "Failed to get guideline content for: {}",
                        fileName,
                        e
                    );
                }
            }

            context.put("project", "stammtisch-logik");
            context.put("guidelines", guidelines);
            context.put("totalGuidelines", guidelines.size());
            context.put("type", "guidelines");
        } catch (Exception e) {
            log.error("Error preparing guideline context", e);
            context.put(
                "error",
                "Failed to prepare guideline context: " + e.getMessage()
            );
        }

        return context;
    }

    /**
     * Search for documents containing specific content
     * @param searchQuery The query to search for
     * @return A map containing matching documents
     */
    public Map<String, Object> searchDocuments(String searchQuery) {
        log.debug("Searching documents for query: {}", searchQuery);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> matchingDocuments = new ArrayList<>();

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            result.put("error", "Search query cannot be empty");
            return result;
        }

        try {
            List<String> allFiles = gitHubService.searchAllMarkdownFiles();
            String lowercaseQuery = searchQuery.toLowerCase();

            for (String path : allFiles) {
                try {
                    String content = gitHubService.getDocumentContent(path);
                    if (
                        content != null &&
                        !content.startsWith("Error:") &&
                        content.toLowerCase().contains(lowercaseQuery)
                    ) {
                        Map<String, Object> match = new HashMap<>();
                        match.put("path", path);
                        match.put("title", extractTitle(path));
                        match.put(
                            "excerpt",
                            createExcerpt(content, searchQuery)
                        );
                        match.put(
                            "url",
                            "https://github.com/mariusreusch/stammtisch-logik/blob/main/" +
                            path
                        );
                        matchingDocuments.add(match);
                    }
                } catch (Exception e) {
                    log.warn("Failed to search in document: {}", path, e);
                }
            }

            result.put("query", searchQuery);
            result.put("matches", matchingDocuments);
            result.put("totalMatches", matchingDocuments.size());
            log.info(
                "Found {} matching documents for query: {}",
                matchingDocuments.size(),
                searchQuery
            );
        } catch (Exception e) {
            log.error("Error searching documents", e);
            result.put(
                "error",
                "Failed to search documents: " + e.getMessage()
            );
        }

        return result;
    }

    /**
     * Extract a title from a file path or name
     */
    private String extractTitle(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.endsWith(".md")) {
            fileName = fileName.substring(0, fileName.length() - 3);
        }
        return fileName.replace("-", " ").replace("_", " ");
    }

    /**
     * Create an excerpt around the search term
     */
    private String createExcerpt(String content, String searchQuery) {
        String lowercaseContent = content.toLowerCase();
        String lowercaseQuery = searchQuery.toLowerCase();

        int index = lowercaseContent.indexOf(lowercaseQuery);
        if (index == -1) {
            return content.length() > 200
                ? content.substring(0, 200) + "..."
                : content;
        }

        int start = Math.max(0, index - 100);
        int end = Math.min(
            content.length(),
            index + searchQuery.length() + 100
        );

        String excerpt = content.substring(start, end);
        if (start > 0) excerpt = "..." + excerpt;
        if (end < content.length()) excerpt = excerpt + "...";

        return excerpt;
    }
}
