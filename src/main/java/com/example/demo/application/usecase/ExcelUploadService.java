package com.example.demo.application.usecase;

import com.example.demo.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ExcelUploadService {

    boolean isExcelValid(MultipartFile file);
    List<User> getUserData(InputStream inputStream);
}
