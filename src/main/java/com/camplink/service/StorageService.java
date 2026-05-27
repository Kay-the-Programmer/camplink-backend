package com.camplink.service;

import com.camplink.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StorageService {

    private final Path uploadRoot;

    public StorageService(@Value("${upload.dir:./uploads}") String uploadDir) throws IOException {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
    }

    public String store(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String filename = UUID.randomUUID() + ext;
        try {
            Path dest = uploadRoot.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw AppException.badRequest("Failed to store file: " + e.getMessage());
        }
    }

    public Path load(String filename) {
        return uploadRoot.resolve(filename).normalize();
    }

    public void delete(String filename) {
        if (filename == null) return;
        try {
            Files.deleteIfExists(uploadRoot.resolve(filename));
        } catch (IOException ignored) {}
    }
}
