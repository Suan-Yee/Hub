package com.example.demo.services.impl;

import com.example.demo.entity.Content;
import com.example.demo.repository.ContentRepository;
import com.example.demo.services.ContentService;
import com.example.demo.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;

    @Override
    public Content createContent() {
        Content newContent=new Content();
        return contentRepository.save(newContent);
    }

    @Override
    public Content findById(Long id) {
        return contentRepository.findById(id).orElse(null);
    }
}
