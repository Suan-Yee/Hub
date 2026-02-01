package com.example.demo.dto;

import com.example.demo.entity.BookMark;

public record BookMarkDto(
        Long id,
        Long userId,
        Long postId,
        boolean status
) {
    public BookMarkDto(BookMark bookMark) {
        this(
                bookMark.getId(),
                bookMark.getUser().getId(),
                bookMark.getPost().getId(),
                bookMark.isStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPostId() {
        return postId;
    }

    public boolean isStatus() {
        return status;
    }
}
