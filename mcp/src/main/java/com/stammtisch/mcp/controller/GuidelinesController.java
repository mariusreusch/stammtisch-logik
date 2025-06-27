package com.stammtisch.mcp.controller;

import com.stammtisch.mcp.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guidelines")
@RequiredArgsConstructor
public class GuidelinesController {

    private final GitHubService gitHubService;

    @GetMapping("/list")
    public ResponseEntity<List<String>> listGuidelineDocuments() {
        return ResponseEntity.ok(gitHubService.listGuidelineDocuments());
    }

    @GetMapping("/document")
    public ResponseEntity<String> getDocumentContent(@RequestParam String path) {
        return ResponseEntity.ok(gitHubService.getDocumentContent(path));
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> searchAllMarkdownFiles() {
        return ResponseEntity.ok(gitHubService.searchAllMarkdownFiles());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "connected",
                "repository", "mariusreusch/stammtisch-logik",
                "service", "MCP Guidelines Reader"
        ));
    }
}
