package com.example.demo.dto;

import com.example.demo.entity.Content;
import com.example.demo.entity.Media;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record ContentDto(
        Long id,
        String text,
        List<MediaDto> media
) {
    public ContentDto(Content content) {
        this(
                content.getId(),
                content.getText(),
                content.getMedia() != null
                        ? content.getMedia().stream().map(MediaDto::new).collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public List<MediaDto> getMedia() { return media; }
}
