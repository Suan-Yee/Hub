package com.example.demo.services;

import com.example.demo.entity.Content;

public interface ContentService {

    Content createContent(String text);

    Content findById(Long id);
}
