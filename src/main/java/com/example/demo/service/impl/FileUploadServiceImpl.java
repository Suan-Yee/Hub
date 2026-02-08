package com.example.demo.service.impl;

import com.example.demo.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        return storeMultipartFile(multipartFile, "images");
    }

    @Override
    public Boolean deleteImage(String imgUrl) throws IOException {
        return deleteLocalFile(imgUrl);
    }

    @Override
    @Async
    public CompletableFuture<String> videoFileUpload(byte[] fileBytes) throws IOException {
        return CompletableFuture.completedFuture(storeBytes(fileBytes, "videos", "bin"));
    }

    @Override
    @Async
    public CompletableFuture<String> imageFileUpload(byte[] fileBytes) throws IOException {
        return CompletableFuture.completedFuture(storeBytes(fileBytes, "images", "bin"));
    }

    @Override
    public String fileUpload(MultipartFile multipartFile) throws IOException {
        return storeMultipartFile(multipartFile, "files");
    }

    @Override
    public String uploadGroupImage(MultipartFile multipartFile) throws IOException {
        return storeMultipartFile(multipartFile, "groups");
    }

    @Override
    public Boolean deleteGroupImage(String imgUrl) throws IOException {
        return deleteImage(imgUrl);
    }

    @Override
    public String uploadVoice(MultipartFile multipartFile) throws IOException {
        return storeMultipartFile(multipartFile, "audio");
    }

    @Override
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        return fileUpload(multipartFile);
    }

    private String storeMultipartFile(MultipartFile multipartFile, String subDir) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IOException("File is empty");
        }

        String originalName = multipartFile.getOriginalFilename();
        String extension = getExtension(originalName);
        String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        Path targetDir = resolveUploadDir(subDir);
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(filename);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + subDir + "/" + filename;
    }

    private String storeBytes(byte[] bytes, String subDir, String extension) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IOException("File is empty");
        }

        String filename = UUID.randomUUID() + (extension == null || extension.isBlank() ? "" : "." + extension);
        Path targetDir = resolveUploadDir(subDir);
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(filename);
        Files.write(targetFile, bytes);
        return "/uploads/" + subDir + "/" + filename;
    }

    private boolean deleteLocalFile(String url) throws IOException {
        if (url == null || url.isBlank()) {
            return false;
        }

        String prefix = "/uploads/";
        int index = url.indexOf(prefix);
        if (index < 0) {
            return false;
        }
        String relativePath = url.substring(index + prefix.length());
        Path filePath = resolveUploadDir("").resolve(relativePath).normalize();
        if (!filePath.startsWith(resolveUploadDir("").normalize())) {
            return false;
        }
        return Files.deleteIfExists(filePath);
    }

    private Path resolveUploadDir(String subDir) {
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (subDir == null || subDir.isBlank()) {
            return base;
        }
        return base.resolve(subDir).normalize();
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1);
    }
}
