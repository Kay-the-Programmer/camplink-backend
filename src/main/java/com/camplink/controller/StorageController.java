package com.camplink.controller;

import com.camplink.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public Map<String, String> upload(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("file") MultipartFile file) {
        String filename = storageService.store(file);
        return Map.of("filename", filename, "path", "/api/files/" + filename);
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = storageService.load(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = filename.toLowerCase().endsWith(".png")
                    ? "image/png" : "image/jpeg";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
