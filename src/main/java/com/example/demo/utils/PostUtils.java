package com.example.demo.utils;

import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class PostUtils {

    public static Post initializePost(CreatePostRequest request, User user) {
        return buildPost(request, user, null);
    }

    public static Post updatePost(CreatePostRequest request, Post existingPost) {
        return buildPost(request, existingPost.getUser(), existingPost);
    }

    private static Post buildPost(CreatePostRequest request, User user, Post existingPost) {
        List<String> tags = Objects.requireNonNullElse(request.tags(), List.of());
        List<String> mentions = Objects.requireNonNullElse(request.mentions(), List.of());
        String visibility = request.visibility() != null && !request.visibility().isBlank()
            ? request.visibility() 
            : "public";
        Boolean edited = Objects.requireNonNullElse(request.edited(), false);

        if (existingPost == null) {
            // Create new post
            return Post.builder()
                .user(user)
                .content(request.content())
                .tags(new ArrayList<>(tags))
                .mentions(new ArrayList<>(mentions))
                .visibility(visibility)
                .edited(edited)
                .likesCount(0)
                .commentsCount(0)
                .sharesCount(0)
                .mediaItems(new ArrayList<>())
                .pollOptions(new HashSet<>())
                .comments(new HashSet<>())
                .reposts(new HashSet<>())
                .bookmarks(new HashSet<>())
                .postHashtags(new HashSet<>())
                .build();
        } else {
            // Update existing post
            if (request.content() != null) {
                existingPost.setContent(request.content());
            }
            if (request.tags() != null) {
                existingPost.setTags(new ArrayList<>(tags));
            }
            if (request.mentions() != null) {
                existingPost.setMentions(new ArrayList<>(mentions));
            }
            if (request.visibility() != null && !request.visibility().isBlank()) {
                existingPost.setVisibility(visibility);
            }
            if (request.edited() != null) {
                existingPost.setEdited(edited);
            }
            return existingPost;
        }
    }
}
