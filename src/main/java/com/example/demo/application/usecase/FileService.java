package com.example.demo.application.usecase;

import com.example.demo.entity.Content;
import com.example.demo.entity.File;

import java.util.List;

public interface FileService {

    File createFile(String fileUrl, Content content);

    List<File> findByContentId(Long id);
}
