package com.camplink.controller;

import com.camplink.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Files", description = "Upload images and serve static files")
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload an image file",
        description = "Accepts a multipart/form-data file upload (JPEG or PNG, max 10 MB). " +
                      "Returns `{ \"filename\": \"uuid.jpg\", \"path\": \"/api/files/uuid.jpg\" }`. " +
                      "Use the `path` value as the `imageUrl` when creating or updating a product."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File uploaded — filename and path returned"),
        @ApiResponse(responseCode = "400", description = "No file provided or unsupported format"),
        @ApiResponse(responseCode = "401", description = "Missing or expired JWT")
    })
    public Map<String, String> upload(
            @AuthenticationPrincipal UserDetails ud,
            @Parameter(description = "Image file (JPEG or PNG, max 10 MB)")
            @RequestParam("file") MultipartFile file) {
        String filename = storageService.store(file);
        return Map.of("filename", filename, "path", "/api/files/" + filename);
    }

    @GetMapping("/files/{filename:.+}")
    @SecurityRequirements   // public — product images must load without auth
    @Operation(
        summary = "Serve an uploaded file",
        description = "Streams the image with `Cache-Control: max-age=86400` (24 h). " +
                      "Content-Type is inferred from the file extension (`.png` → `image/png`, others → `image/jpeg`). " +
                      "No authentication required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Image returned",
                     content = @Content(mediaType = "image/jpeg")),
        @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "Filename returned by the upload endpoint")
            @PathVariable String filename) {
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
