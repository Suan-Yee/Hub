package com.example.demo.utils;

import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostMention;
import com.example.demo.entity.PostTag;
import com.example.demo.entity.User;

import java.util.ArrayList;
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
            Post post = Post.builder()
                .user(user)
                .content(request.content())
                .visibility(visibility)
                .edited(edited)
                .likesCount(0)
                .commentsCount(0)
                .sharesCount(0)
                .build();
            replaceTags(post, tags);
            replaceMentions(post, mentions);
            return post;
        } else {
            // Update existing post
            if (request.content() != null) {
                existingPost.setContent(request.content());
            }
            if (request.tags() != null) {
                replaceTags(existingPost, tags);
            }
            if (request.mentions() != null) {
                replaceMentions(existingPost, mentions);
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

    private static void replaceTags(Post post, List<String> tags) {
        post.getTags().clear();
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            post.getTags().add(PostTag.builder()
                .post(post)
                .tag(tag)
                .build());
        }
    }

    private static void replaceMentions(Post post, List<String> mentions) {
        post.getMentions().clear();
        for (String mention : mentions) {
            if (mention == null || mention.isBlank()) {
                continue;
            }
            post.getMentions().add(PostMention.builder()
                .post(post)
                .username(mention)
                .build());
        }
    }
}
