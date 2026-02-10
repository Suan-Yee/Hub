package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreatePostRequest(
    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    String content,
    
    List<String> tags,
    List<String> mentions,
    List<MediaItemRequest> media,
    PollRequest poll,
    Long groupId,
    @NotBlank
    String visibility ,// 'public', 'followers', 'me'
    Boolean edited
) {
}
