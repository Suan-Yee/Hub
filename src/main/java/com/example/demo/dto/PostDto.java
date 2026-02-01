package com.example.demo.dto;

import com.example.demo.entity.Post;

import java.util.List;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;

public record PostDto(
        Long id,
        boolean status,
        ContentDto content,
        TopicDto topic,
        UserDto user,
        int likes,
        boolean isLikedByCurrentUser,
        boolean isBookMark,
        String photo,
        String time,
        Long commentCount,
        String groupName,
        boolean owner,
        boolean isAdmin,
        List<MentionDto> mentionUserList,
        List<String> topicList
) {
    public PostDto(Post post) {
        this(
                post.getId(),
                post.isStatus(),
                new ContentDto(post.getContent()),
                post.getTopic() != null ? new TopicDto(post.getTopic()) : null,
                new UserDto(post.getUser()),
                0,
                false,
                false,
                post.getUser().getPhoto(),
                formatTimeAgo(post.getCreatedAt()),
                null,
                post.getGroup() != null ? post.getGroup().getName() : null,
                false,
                false,
                List.of(),
                List.of()
        );
    }

    public PostDto withLikes(int value) {
        return new PostDto(id, status, content, topic, user, value, isLikedByCurrentUser, isBookMark, photo, time,
                commentCount, groupName, owner, isAdmin, mentionUserList, topicList);
    }

    public PostDto withLikedByCurrentUser(boolean value) {
        return new PostDto(id, status, content, topic, user, likes, value, isBookMark, photo, time, commentCount,
                groupName, owner, isAdmin, mentionUserList, topicList);
    }

    public PostDto withBookMark(boolean value) {
        return new PostDto(id, status, content, topic, user, likes, isLikedByCurrentUser, value, photo, time,
                commentCount, groupName, owner, isAdmin, mentionUserList, topicList);
    }

    public PostDto withCommentCount(Long value) {
        return new PostDto(id, status, content, topic, user, likes, isLikedByCurrentUser, isBookMark, photo, time,
                value, groupName, owner, isAdmin, mentionUserList, topicList);
    }

    public PostDto withMentionUserList(List<MentionDto> value) {
        return new PostDto(id, status, content, topic, user, likes, isLikedByCurrentUser, isBookMark, photo, time,
                commentCount, groupName, owner, isAdmin, value, topicList);
    }

    public PostDto withTopicList(List<String> value) {
        return new PostDto(id, status, content, topic, user, likes, isLikedByCurrentUser, isBookMark, photo, time,
                commentCount, groupName, owner, isAdmin, mentionUserList, value);
    }

    public PostDto withOwner(boolean value) {
        return new PostDto(id, status, content, topic, user, likes, isLikedByCurrentUser, isBookMark, photo, time,
                commentCount, groupName, value, isAdmin, mentionUserList, topicList);
    }

    public PostDto withAdmin(boolean value) {
        return new PostDto(id, status, content, topic, user, likes, isLikedByCurrentUser, isBookMark, photo, time,
                commentCount, groupName, owner, value, mentionUserList, topicList);
    }

    public Long getId() {
        return id;
    }

    public boolean isStatus() {
        return status;
    }

    public ContentDto getContent() {
        return content;
    }

    public TopicDto getTopic() {
        return topic;
    }

    public UserDto getUser() {
        return user;
    }

    public int getLikes() {
        return likes;
    }

    public String getPhoto() {
        return photo;
    }

    public String getTime() {
        return time;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public List<MentionDto> getMentionUserList() {
        return mentionUserList;
    }

    public List<String> getTopicList() {
        return topicList;
    }
}
