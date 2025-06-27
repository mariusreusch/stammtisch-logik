package com.stammtisch.mcp.controller;

import com.stammtisch.mcp.service.GitHubService;
import io.modelcontextprotocol.sdk.ModelContextServer;
import io.modelcontextprotocol.sdk.search.SearchQuery;
import io.modelcontextprotocol.sdk.search.SearchResponse;
import io.modelcontextprotocol.sdk.search.SearchResult;
import io.modelcontextprotocol.sdk.search.SearchResultsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
@Slf4j
public class McpController {

    private final GitHubService gitHubService;
    private final ModelContextServer server = ModelContextServer.builder().build();

    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestBody SearchQuery query) {
        log.info("Received search query: {}", query.getQuery());

        // Register a handler that uses our GitHubService to search for documents
        server.registerSearchResultsHandler(new SearchResultsHandler() {
            @Override
            public List<SearchResult> search(String query) {
                // Convert our document search results to MCP SearchResult format
                return gitHubService.searchAllMarkdownFiles().stream()
                    .map(path -> {
                        String content = gitHubService.getDocumentContent(path);
                        return SearchResult.builder()
                                .title(path)
                                .content(content)
                                .url("https://github.com/mariusreusch/stammtisch-logik/blob/main/" + path)
                                .build();
                    })
                    .collect(Collectors.toList());
            }
        });

        // Use the MCP server to handle the search
        return ResponseEntity.ok(server.handleSearch(query));
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("MCP Server is running - ModelContextProtocol/java-sdk integration complete");
    }
}
