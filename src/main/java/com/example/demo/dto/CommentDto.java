package com.example.demo.dto;

import com.example.demo.entity.Comment;

import java.util.List;
import java.util.Objects;

public record CommentDto(
        Long id,
        Long userId,
        Long postId,
        String text,
        String userName,
        String userImage,
        Long parentCommentId,
        String parentUserName,
        Long rootCommentId,
        Long childComment,
        String time,
        boolean isEdited,
        boolean isOwner,
        boolean isCommentLiked,
        Long totalLike,
        List<String> mention,
        List<MentionDto> mentionUserList,
        boolean replyToIsOwnComment
) {
    public CommentDto(Comment comment) {
        this(
                comment.getId(),
                comment.getUser().getId(),
                comment.getPost().getId(),
                comment.getText(),
                comment.getUser().getName(),
                comment.getUser().getPhoto(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getParentComment() != null ? comment.getParentComment().getUser().getName() : null,
                comment.getRootComment() != null ? comment.getRootComment().getId() : null,
                null,
                null,
                comment.isIsEdited(),
                false,
                false,
                null,
                List.of(),
                List.of(),
                comment.getParentComment() != null
                        && Objects.equals(comment.getParentComment().getUser().getStaffId(), comment.getUser().getStaffId())
        );
    }

    public CommentDto withChildComment(Long value) {
        return new CommentDto(id, userId, postId, text, userName, userImage, parentCommentId, parentUserName,
                rootCommentId, value, time, isEdited, isOwner, isCommentLiked, totalLike, mention, mentionUserList,
                replyToIsOwnComment);
    }

    public CommentDto withTime(String value) {
        return new CommentDto(id, userId, postId, text, userName, userImage, parentCommentId, parentUserName,
                rootCommentId, childComment, value, isEdited, isOwner, isCommentLiked, totalLike, mention,
                mentionUserList, replyToIsOwnComment);
    }

    public CommentDto withOwner(boolean value) {
        return new CommentDto(id, userId, postId, text, userName, userImage, parentCommentId, parentUserName,
                rootCommentId, childComment, time, isEdited, value, isCommentLiked, totalLike, mention, mentionUserList,
                replyToIsOwnComment);
    }

    public CommentDto withCommentLiked(boolean value) {
        return new CommentDto(id, userId, postId, text, userName, userImage, parentCommentId, parentUserName,
                rootCommentId, childComment, time, isEdited, isOwner, value, totalLike, mention, mentionUserList,
                replyToIsOwnComment);
    }

    public CommentDto withTotalLike(Long value) {
        return new CommentDto(id, userId, postId, text, userName, userImage, parentCommentId, parentUserName,
                rootCommentId, childComment, time, isEdited, isOwner, isCommentLiked, value, mention, mentionUserList,
                replyToIsOwnComment);
    }

    public CommentDto withMentionUserList(List<MentionDto> value) {
        return new CommentDto(id, userId, postId, text, userName, userImage, parentCommentId, parentUserName,
                rootCommentId, childComment, time, isEdited, isOwner, isCommentLiked, totalLike, mention, value,
                replyToIsOwnComment);
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

    public String getText() {
        return text;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public String getParentUserName() {
        return parentUserName;
    }

    public Long getRootCommentId() {
        return rootCommentId;
    }

    public Long getChildComment() {
        return childComment;
    }

    public String getTime() {
        return time;
    }

    public Long getTotalLike() {
        return totalLike;
    }

    public List<String> getMention() {
        return mention;
    }

    public List<MentionDto> getMentionUserList() {
        return mentionUserList;
    }
}
