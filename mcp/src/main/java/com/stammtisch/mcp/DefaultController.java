package com.stammtisch.mcp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
public class DefaultController {
    @GetMapping
    public ResponseEntity<Void> root() {
        return ResponseEntity.ok().build();
    }
}

