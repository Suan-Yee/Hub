package com.example.demo.application.usecase;

import com.example.demo.entity.Content;

public interface ContentService {

    Content createContent(String text);

    Content findById(Long id);

    void updateContent(Long id, String text);

    Content save(Content content);
}
