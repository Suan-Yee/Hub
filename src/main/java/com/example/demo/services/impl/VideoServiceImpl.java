package com.example.demo.services.impl;

import com.example.demo.entity.Content;
import com.example.demo.entity.Video;
import com.example.demo.repository.VideoRepository;
import com.example.demo.services.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;

    @Override
    public Video createVideo(String videoURL, Content content) {
        Video newVideo=new Video();
        newVideo.setName(videoURL);
        newVideo.setContent(content);
        return videoRepository.save(newVideo);
    }

    @Override
    public Video findByContentId(Long id) {
        return videoRepository.findByContent_Id(id);
    }

    @Override
    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteVideoByUrl(String videoUrl, Long contentId) {
        videoRepository.deleteByContentId(videoUrl,contentId);
    }
}
