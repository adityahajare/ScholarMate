package com.scholarmate.service;

import com.scholarmate.entity.ProfileDocument;
import com.scholarmate.entity.UserProfile;
import com.scholarmate.repository.ProfileDocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class ProfileDocumentStorageService {
    private static final long MAX_SIZE = 10_485_760L;
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of("application/pdf", "image/jpeg", "image/png");

    private final ProfileDocumentRepository documents;
    private final Path root;

    public ProfileDocumentStorageService(
            ProfileDocumentRepository documents,
            @Value("${scholarmate.documents.storage-dir:./data/profile-documents}") String storageDirectory) {
        this.documents = documents;
        this.root = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public ProfileDocument store(UserProfile profile, String documentType, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("A document file is required.");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("Document must be at most 10 MB.");
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(contentType)) throw new IllegalArgumentException("Only PDF, JPG and PNG documents are allowed.");
        try {
            String digest = digest(file.getInputStream());
            Path userDirectory = root.resolve(String.valueOf(profile.getId())).normalize();
            Files.createDirectories(userDirectory);
            String safeName = safeFileName(file.getOriginalFilename());
            String storedName = digest + "-" + safeName;
            Path target = userDirectory.resolve(storedName).normalize();
            if (!target.startsWith(userDirectory)) throw new IllegalArgumentException("Invalid document name.");
            if (!Files.exists(target)) Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            ProfileDocument document = ProfileDocument.builder()
                    .userProfile(profile)
                    .documentType(documentType)
                    .fileName(safeName)
                    .contentType(contentType)
                    .sizeBytes(file.getSize())
                    .storageKey(profile.getId() + "/" + storedName)
                    .status("UPLOADED")
                    .build();
            return documents.save(document);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store the document.", exception);
        }
    }

    public Resource load(ProfileDocument document) {
        try {
            Path target = root.resolve(document.getStorageKey()).normalize();
            if (!target.startsWith(root) || !Files.exists(target)) throw new IllegalArgumentException("Document file not found.");
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) throw new IllegalArgumentException("Document file is not readable.");
            return resource;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the document.", exception);
        }
    }

    public void deleteFile(ProfileDocument document) {
        if (document.getStorageKey() == null) return;
        try { Files.deleteIfExists(root.resolve(document.getStorageKey()).normalize()); }
        catch (IOException exception) { throw new IllegalStateException("Could not delete the document file.", exception); }
    }

    private String digest(InputStream input) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) digest.update(buffer, 0, read);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 is unavailable.", exception); }
    }

    private String safeFileName(String original) {
        String name = original == null ? "document" : Path.of(original).getFileName().toString();
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return name.isBlank() ? "document" : name;
    }
}
