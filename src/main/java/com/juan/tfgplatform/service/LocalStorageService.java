package com.juan.tfgplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    @Value("${uploads.path:./uploads}")
    private String uploadPath;

    @Override
    public String store(MultipartFile file) {
        try {
            Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + extension;

            Path destination = dir.resolve(filename);
            file.transferTo(destination);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Path file = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // log but don't throw — deletion failure is non-critical
        }
    }
}
