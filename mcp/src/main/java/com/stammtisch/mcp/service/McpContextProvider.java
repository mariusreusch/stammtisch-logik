package com.stammtisch.mcp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpContextProvider {

    private final GitHubService gitHubService;


    @Tool(description = "Get all stammtisch guidelines, which describes good practices in the area of software design.")
    public String getAllStammtischGuidelines() {
        log.info("Getting guideline context");

        Map<String, Object> context = new HashMap<>();
        List<Map<String, String>> guidelines = new ArrayList<>();

        StringBuilder wholeContent = new StringBuilder();

        try {
            List<String> guidelineFiles =
                gitHubService.listGuidelineDocuments();
            log.info("Found {} guideline files", guidelineFiles.size());

            for (String fileName : guidelineFiles) {
                try {
                    String path = "doc/" + fileName;
                    String content = gitHubService.getDocumentContent(path);
                    wholeContent.append(content);
                    if (content != null && !content.startsWith("Error:")) {
                        Map<String, String> guideline = new HashMap<>();
                        guideline.put("fileName", fileName);
                        guideline.put("path", path);
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

        return wholeContent.toString();
    }

}
