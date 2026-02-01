package com.example.demo.dto;

import java.time.LocalDateTime;

public record LikeDto(
        Long id,
        LocalDateTime date,
        boolean status,
        Long postId,
        Long commentId
) {
    public Long getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean isStatus() {
        return status;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getCommentId() {
        return commentId;
    }
}
