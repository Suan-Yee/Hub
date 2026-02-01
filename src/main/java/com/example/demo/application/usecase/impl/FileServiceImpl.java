package com.example.demo.application.usecase.impl;

import com.example.demo.entity.Content;
import com.example.demo.entity.File;
import com.example.demo.infrastructure.persistence.repository.FileRepository;
import com.example.demo.application.usecase.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Override
    public File createFile(String fileUrl, Content content) {
        File newFile = new File();
        newFile.setName(fileUrl);
        newFile.setContent(content);
        return fileRepository.save(newFile);
    }

    @Override
    public List<File> findByContentId(Long id) {
        return fileRepository.findByContent_Id(id);
    }
}
