package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePostRequest(
    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    String content,
    
    List<String> tags,
    List<String> mentions,
    List<MultipartFile> mediaFiles,
    PollRequest poll,
    Long groupId,
    String visibility // 'public', 'followers', 'me'
) {
    public record PollRequest(
        @NotBlank(message = "Poll question is required")
        String question,
        
        @Size(min = 2, max = 10, message = "Poll must have between 2 and 10 options")
        List<String> options
    ) {}
}
