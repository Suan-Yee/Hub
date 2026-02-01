package com.example.demo.application.usecase;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface FileUploadService {

    String uploadImage(MultipartFile multipartFile) throws IOException;
    Boolean deleteImage(String imgUrl) throws IOException;
    CompletableFuture<String> videoFileUpload(byte[] fileBytes) throws IOException;
    CompletableFuture<String> imageFileUpload(byte[] fileBytes) throws IOException;
    String fileUpload(MultipartFile multipartFile) throws IOException;
    String uploadGroupImage(MultipartFile multipartFile) throws IOException;
    Boolean deleteGroupImage(String imgUrl) throws IOException;
    String uploadVoice(MultipartFile multipartFile) throws IOException;
    String uploadFile(MultipartFile multipartFile) throws IOException;
}
