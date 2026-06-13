package com.camplink.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.camplink.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

/**
 * Stores uploaded images and returns the reference to persist as a product /
 * avatar image.
 *
 * <p>Two backends:
 * <ul>
 *   <li><b>Cloudinary</b> — used when {@code CLOUDINARY_URL} is configured.
 *       Returns a full {@code https://res.cloudinary.com/...} URL. Persistent
 *       and CDN-served — the right choice in production.</li>
 *   <li><b>Local disk</b> — fallback for local dev. Returns a host-relative
 *       {@code /api/files/<uuid>.<ext>} path served by {@code StorageController}.
 *       Ephemeral on Render's free tier, so not for production.</li>
 * </ul>
 * The returned string is stored verbatim as the image reference; the Flutter
 * client resolves it with {@code ApiClient.fileUrl}, which passes absolute URLs
 * through unchanged and joins relative paths against the API host.</p>
 */
@Service
@Slf4j
public class StorageService {

    private final Path uploadRoot;
    private final Cloudinary cloudinary; // null when not configured

    public StorageService(@Value("${upload.dir:./uploads}") String uploadDir,
                          @Value("${cloudinary.url:}") String cloudinaryUrl) throws IOException {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
        this.cloudinary = initCloudinary(cloudinaryUrl);
    }

    private Cloudinary initCloudinary(String url) {
        // Prefer the explicit property; the Cloudinary SDK also reads the
        // CLOUDINARY_URL env var directly, but being explicit keeps the
        // behaviour obvious and testable.
        try {
            if (url != null && !url.isBlank()) {
                Cloudinary c = new Cloudinary(url.trim());
                log.info("Cloudinary image storage enabled (cloud: {}).",
                        c.config.cloudName);
                return c;
            }
        } catch (Exception e) {
            log.warn("Cloudinary init failed ({}). Falling back to local disk storage.",
                    e.getMessage());
        }
        log.info("Cloudinary not configured — using local disk storage at {} "
                + "(ephemeral on Render).", uploadRoot);
        return null;
    }

    public boolean isCloudinary() {
        return cloudinary != null;
    }

    /**
     * Stores the file and returns the value to persist as the image reference:
     * a full Cloudinary URL, or a host-relative {@code /api/files/...} path.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw AppException.badRequest("No file provided");
        }
        return cloudinary != null
                ? storeCloudinary(file)
                : "/api/files/" + storeLocal(file);
    }

    private String storeCloudinary(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "camplink",
                            "resource_type", "image"));
            Object url = result.get("secure_url");
            if (url == null) {
                throw new IllegalStateException("Cloudinary returned no secure_url");
            }
            return url.toString();
        } catch (Exception e) {
            throw AppException.badRequest("Failed to upload image: " + e.getMessage());
        }
    }

    private String storeLocal(MultipartFile file) {
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
