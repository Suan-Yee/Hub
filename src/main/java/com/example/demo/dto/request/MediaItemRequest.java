package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MediaItemRequest(
    @NotBlank(message = "Media type is required")
    String type, // 'image' or 'video'
    @NotBlank(message = "Media url is required")
    String url
) {
}
