package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExcelUploadService {
    
    void updateUsers(MultipartFile file) throws IOException;
}
