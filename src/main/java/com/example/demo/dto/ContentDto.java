package com.example.demo.dto;

import com.example.demo.entity.Content;
import com.example.demo.entity.File;
import com.example.demo.entity.Image;
import com.example.demo.entity.Video;

import java.util.List;

public record ContentDto(
        Long id,
        String text,
        List<Video> videos,
        List<File> files,
        List<Image> images
) {
    public ContentDto(Content content) {
        this(
                content.getId(),
                content.getText(),
                content.getVideos(),
                content.getFiles(),
                content.getImages()
        );
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public List<File> getFiles() {
        return files;
    }

    public List<Image> getImages() {
        return images;
    }
}
