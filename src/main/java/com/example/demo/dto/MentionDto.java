package com.example.demo.dto;

import com.example.demo.entity.Mention;

public record MentionDto(
        Long userId,
        String userName,
        Long commentId,
        Long postId,
        String userStaffId
) {
    public MentionDto(Mention mention) {
        this(
                mention.getUser() != null ? mention.getUser().getId() : null,
                mention.getUser() != null ? mention.getUser().getName() : null,
                mention.getComment() != null ? mention.getComment().getId() : null,
                mention.getPost() != null ? mention.getPost().getId() : null,
                mention.getUser() != null ? mention.getUser().getStaffId() : null
        );
    }

    public MentionDto withUserStaffId(String value) {
        return new MentionDto(userId, userName, commentId, postId, value);
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Long getCommentId() {
        return commentId;
    }

    public Long getPostId() {
        return postId;
    }

    public String getUserStaffId() {
        return userStaffId;
    }
}
