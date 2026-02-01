package com.example.demo.application.usecase.impl;

import com.example.demo.entity.Content;
import com.example.demo.infrastructure.persistence.repository.ContentRepository;
import com.example.demo.application.usecase.ContentService;
import com.example.demo.application.usecase.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;

    @Override
    public Content createContent(String text) {
        Content newContent=new Content();
        newContent.setText(text);
        return contentRepository.save(newContent);
    }

    @Override
    public Content findById(Long id) {
        return contentRepository.findById(id).orElse(null);
    }

    @Override
    public void updateContent(Long id, String text) {
        Content exitContent=contentRepository.findById(id).orElse(null);
        exitContent.setText(text);
        contentRepository.save(exitContent);
    }

    @Override
    public Content save(Content content) {
        return contentRepository.save(content);
    }
}
