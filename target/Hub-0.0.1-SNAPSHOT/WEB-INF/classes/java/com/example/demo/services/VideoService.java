package com.example.demo.services;

import com.example.demo.entity.Content;
import com.example.demo.entity.Video;

public interface VideoService {

    Video createVideo(String videoURL, Content content);

    Video findByContentId(Long id);

    void deleteVideo(Long id);
}
