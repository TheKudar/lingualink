package com.lingualink.common.storage;

import com.lingualink.common.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final Path UPLOAD_ROOT = Path.of("uploads").toAbsolutePath().normalize();

    public String saveImage(MultipartFile file, String folder) {
        validateImage(file);

        String extension = resolveExtension(file);
        String filename = UUID.randomUUID() + extension;
        Path targetDirectory = UPLOAD_ROOT.resolve(folder).normalize();
        Path targetFile = targetDirectory.resolve(filename).normalize();

        if (!targetFile.startsWith(UPLOAD_ROOT)) {
            throw new AppException("Invalid upload path");
        }

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new AppException("Could not save uploaded file");
        }

        return "/uploads/" + folder + "/" + filename;
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException("File size must be 5MB or less");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new AppException("Only JPG and PNG files are allowed");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        return ".jpg";
    }
}
