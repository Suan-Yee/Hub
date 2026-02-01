package com.example.demo.dto;

import com.example.demo.entity.Media;
import com.example.demo.enumeration.MediaType;

public record MediaDto(Long id, String url, MediaType type) {

    public MediaDto(Media media) {
        this(media.getId(), media.getUrl(), media.getType());
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public MediaType getType() { return type; }
}
