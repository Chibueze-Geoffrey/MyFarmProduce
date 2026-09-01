package com.myfarmproduce.web.service;

import com.myfarmproduce.application.service.FileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Stores uploaded images under {uploadRoot}/{folder} and returns a relative URL. */
@Component
public class LocalFileStorageImpl implements FileStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path uploadRoot;

    public LocalFileStorageImpl(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir);
    }

    @Override
    public String saveImage(InputStream content, String originalFileName, String folder) {
        String original = originalFileName == null ? "" : originalFileName;
        int dot = original.lastIndexOf('.');
        String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext))
            throw new IllegalStateException("Only image files (jpg, png, gif, webp) are allowed.");

        try {
            Path dir = uploadRoot.resolve(folder);
            Files.createDirectories(dir);

            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(fileName);
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + folder + "/" + fileName;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
