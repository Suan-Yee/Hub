package com.example.demo.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
                ObjectUtils.asMap("resource_type", "image"));
        return uploadResult.get("url").toString();
    }

    @Override
    public Boolean deleteImage(String imgUrl) throws IOException {
        if (imgUrl == null || imgUrl.isEmpty()) {
            return false;
        }
        
        String publicId = extractPublicId(imgUrl);
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        return "ok".equals(result.get("result"));
    }

    @Override
    @Async
    public CompletableFuture<String> videoFileUpload(byte[] fileBytes) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(fileBytes,
                ObjectUtils.asMap("resource_type", "video"));
        return CompletableFuture.completedFuture(uploadResult.get("url").toString());
    }

    @Override
    @Async
    public CompletableFuture<String> imageFileUpload(byte[] fileBytes) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(fileBytes,
                ObjectUtils.asMap("resource_type", "image"));
        return CompletableFuture.completedFuture(uploadResult.get("url").toString());
    }

    @Override
    public String fileUpload(MultipartFile multipartFile) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
                ObjectUtils.asMap("resource_type", "raw"));
        return uploadResult.get("url").toString();
    }

    @Override
    public String uploadGroupImage(MultipartFile multipartFile) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
                ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", "groups"
                ));
        return uploadResult.get("url").toString();
    }

    @Override
    public Boolean deleteGroupImage(String imgUrl) throws IOException {
        return deleteImage(imgUrl);
    }

    @Override
    public String uploadVoice(MultipartFile multipartFile) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
                ObjectUtils.asMap("resource_type", "video")); // Cloudinary uses 'video' for audio
        return uploadResult.get("url").toString();
    }

    @Override
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        return fileUpload(multipartFile);
    }

    /**
     * Extract public ID from Cloudinary URL
     */
    private String extractPublicId(String url) {
        // Extract public_id from URL like: https://res.cloudinary.com/xxx/image/upload/v123456/public_id.jpg
        String[] parts = url.split("/upload/");
        if (parts.length > 1) {
            String afterUpload = parts[1];
            // Remove version number (v123456/)
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            // Remove extension
            int dotIndex = withoutVersion.lastIndexOf('.');
            if (dotIndex > 0) {
                return withoutVersion.substring(0, dotIndex);
            }
            return withoutVersion;
        }
        return url;
    }
}
