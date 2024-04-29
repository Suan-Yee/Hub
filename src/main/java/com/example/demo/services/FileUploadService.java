package com.example.demo.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {

    String uploadImage(MultipartFile multipartFile) throws IOException;
    Boolean deleteImage(String imgUrl) throws IOException;
    String videoFileUpload(MultipartFile multipartFile) throws IOException;
    String imageFileUpload(MultipartFile multipartFile) throws IOException;
    String fileUpload(MultipartFile multipartFile) throws IOException;
}
