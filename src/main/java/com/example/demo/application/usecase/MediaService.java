package com.example.demo.application.usecase;

import com.example.demo.entity.Content;
import com.example.demo.entity.Media;
import com.example.demo.enumeration.MediaType;

import java.util.List;

public interface MediaService {

    Media createMedia(String url, MediaType type, Content content);

    List<Media> findByContentId(Long contentId);

    void deleteById(Long id);

    void deleteByUrl(String url, Long contentId);
}
