package com.example.demo.application.usecase.impl;

import com.example.demo.entity.Content;
import com.example.demo.entity.Media;
import com.example.demo.enumeration.MediaType;
import com.example.demo.application.usecase.MediaService;
import com.example.demo.infrastructure.persistence.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    @Override
    public Media createMedia(String url, MediaType type, Content content) {
        Media media = Media.builder()
                .url(url)
                .type(type)
                .content(content)
                .build();
        return mediaRepository.save(media);
    }

    @Override
    public List<Media> findByContentId(Long contentId) {
        return mediaRepository.findByContent_Id(contentId);
    }

    @Override
    public void deleteById(Long id) {
        mediaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByUrl(String url, Long contentId) {
        mediaRepository.deleteByContentIdAndUrl(contentId, url);
    }
}
