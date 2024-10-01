package com.example.demo.services.impl;

import com.cloudinary.Cloudinary;
import com.example.demo.entity.User;
import com.example.demo.services.FileUploadService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@EnableAsync
public class FileUploadServiceImpl implements FileUploadService {
    private final Cloudinary cloudinary;
    private final UserService userService;

    public String extractPublicId(String imgUrl){
        int startIndex = imgUrl.lastIndexOf("/")+1;
        int endIndex = imgUrl.lastIndexOf(".");
        return imgUrl.substring(startIndex,endIndex);
    }

    @Override
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        if(user.getPhoto()!=null && !user.getPhoto().isEmpty()){
            deleteImage(user.getPhoto());
        }
        return cloudinary.uploader()
                .upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()))
                .get("url")
                .toString();
    }

    @Override
    public Boolean deleteImage(String imgUrl) throws IOException {
        String publicId = extractPublicId(imgUrl);
        cloudinary.uploader().destroy(publicId,null);
        return true;
    }

    @Async
    public CompletableFuture<String> videoFileUpload(byte[] fileBytes) throws IOException {
        return uploadVideoFile(fileBytes);
    }

    private CompletableFuture<String> uploadVideoFile(byte[] fileBytes) {
        try {
            String url = cloudinary.uploader().uploadLarge(fileBytes,
                            Map.of("public_id", UUID.randomUUID().toString(), "resource_type", "video"))
                    .get("url").toString();
            return CompletableFuture.completedFuture(url);
        } catch (IOException e) {
            // Handle the exception properly here
            return CompletableFuture.failedFuture(e);
        }
    }
    @Async
    @Override
    public CompletableFuture<String> imageFileUpload(byte[] fileBytes) throws IOException {
        String url = cloudinary.uploader().upload(fileBytes,
                        Map.of("public_id", UUID.randomUUID().toString()))
                .get("url").toString();
        return CompletableFuture.completedFuture(url);
    }

    @Override
    public String fileUpload(MultipartFile multipartFile) throws IOException {
        return cloudinary.uploader().upload(multipartFile.getBytes(),
                        Map.of("public_id", UUID.randomUUID().toString()))
                .get("url").toString();
    }

    @Override
    public String uploadGroupImage(MultipartFile multipartFile) throws IOException {
        return cloudinary.uploader()
                .upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()))
                .get("url")
                .toString();
    }

    @Override
    public Boolean deleteGroupImage(String imgUrl) throws IOException {
        String publicId = extractPublicId(imgUrl);
        cloudinary.uploader().destroy(publicId, null);
        return true;
    }
    @Override
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        return cloudinary.uploader().upload(multipartFile.getBytes(), Map.of("public_id", UUID.randomUUID().toString()))
                .get("url").toString();
    }
    @Override
    public String uploadVoice(MultipartFile multipartFile) throws IOException {
        String publicId = "voice/" + UUID.randomUUID().toString();
        Map<String, Object> options = Map.of(
                "resource_type", "video",
                "public_id", publicId
        );
        Map<String, Object> uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(), options);
        return uploadResult.get("url").toString();
    }

}
