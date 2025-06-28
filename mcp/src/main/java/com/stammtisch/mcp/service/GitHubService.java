package com.stammtisch.mcp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GitHubService {

    private static final String REPO_URL = "mariusreusch/stammtisch-logik";
    private static final String DEFAULT_BRANCH = "main";
    private final GitHub gitHub;
    private final GHRepository repository;

    public GitHubService() throws IOException {
        // Connect to GitHub using anonymous connection (for public repositories)
        this.gitHub = GitHub.connectAnonymously();
        this.repository = gitHub.getRepository(REPO_URL);
        log.info("Successfully connected to GitHub repository: {}", REPO_URL);
    }

    /**
     * List all markdown files in the repository that might contain guidelines
     */
    public List<String> listGuidelineDocuments() {
        try {
            List<GHContent> content = repository.getDirectoryContent(
                "doc",
                DEFAULT_BRANCH
            );
            return content
                .stream()
                .filter(file -> file.getName().endsWith(".md"))
                .map(GHContent::getName)
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to list guideline documents", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get the content of a specific markdown file
     */
    public String getDocumentContent(String path) {
        try {
            GHContent content = repository.getFileContent(path, DEFAULT_BRANCH);
            byte[] contentBytes = Base64.getDecoder()
                .decode(content.getContent());
            return new String(contentBytes);
        } catch (IOException e) {
            log.error("Failed to get document content for path: {}", path, e);
            return "Error: Could not retrieve document content";
        }
    }

    /**
     * Search for all markdown files in the repository
     */
    public List<String> searchAllMarkdownFiles() {
        try {
            GHContentSearchBuilder search = gitHub.searchContent();
            search.repo(REPO_URL);
            search.filename("*.md");

            List<String> paths = new ArrayList<>();
            for (GHContent content : search.list()) {
                paths.add(content.getPath());
            }

            log.info("Found {} markdown files in repository", paths.size());
            return paths;
        } catch (Exception e) {
            log.error("Failed to search markdown files", e);
            return Collections.emptyList();
        }
    }
}
