package com.example.demo.service.impl;

import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ApiException;
import com.example.demo.mapper.PostMapper;
import com.example.demo.repository.*;
import com.example.demo.service.FileUploadService;
import com.example.demo.service.PostService;
import com.example.demo.utils.PostUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PollOptionRepository pollOptionRepository;
    private final FileUploadService fileUploadService;
    private final PostMapper postMapper;

    @Override
    public PostResponse createPost(CreatePostRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException("User not found"));
            
        Post post = PostUtils.initializePost(request, user);
        handleGroupAssociation(request, post);
        processPostTypeAndMedia(request, post, null);

        post = postRepository.save(post);

        if (request.poll() != null) {
            savePollOptions(post, request.poll().options());
        }
        
        return postMapper.mapToPostResponse(post, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));
        return postMapper.mapToPostResponse(post, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUserId(Long userId, Long currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        return posts.map(post -> postMapper.mapToPostResponse(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPublicFeed(Long currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findPublicPosts(pageable);
        return posts.map(post -> postMapper.mapToPostResponse(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByGroupId(Long groupId, Long currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findByGroupId(groupId, pageable);
        return posts.map(post -> postMapper.mapToPostResponse(post, currentUserId));
    }

    @Override
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));

        validatePostOwnership(post, userId);
        deleteOldMedia(post);
        postRepository.delete(post);
    }

    @Override
    public PostResponse updatePost(Long postId, CreatePostRequest request, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));

        validatePostOwnership(post, userId);

        // Update post fields
        post.setContent(request.content());
        post.setTags(request.tags() != null ? request.tags() : new ArrayList<>());
        post.setMentions(request.mentions() != null ? request.mentions() : new ArrayList<>());

        // Handle media updates (JSON-only: media items are already uploaded elsewhere)
        if (request.media() != null) {
            deleteOldMedia(post);
        }

        processPostTypeAndMedia(request, post, post.getType());

        post = postRepository.save(post);
        return postMapper.mapToPostResponse(post, userId);
    }

    // Helper methods

    private void validatePostOwnership(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new ApiException("You are not authorized to perform this action on this post");
        }
    }

    private void deleteOldMedia(Post post) {
        if (post.getMediaItems() != null && !post.getMediaItems().isEmpty()) {
            post.getMediaItems().forEach(mediaItem -> {
                try {
                    fileUploadService.deleteImage(mediaItem.getUrl());
                } catch (IOException e) {
                    log.error("Failed to delete media file: {}", mediaItem.getUrl(), e);
                }
            });
        }
    }

    private List<Post.MediaItem> mapMediaItemsFromRequest(List<CreatePostRequest.MediaItemRequest> media) {
        if (media == null || media.isEmpty()) {
            return new ArrayList<>();
        }

        List<Post.MediaItem> mediaItems = new ArrayList<>();
        for (CreatePostRequest.MediaItemRequest item : media) {
            mediaItems.add(new Post.MediaItem(
                UUID.randomUUID().toString(),
                item.type(),
                item.url()
            ));
        }
        return mediaItems;
    }

    private boolean hasVideo(List<CreatePostRequest.MediaItemRequest> media) {
        return media.stream().anyMatch(item -> "video".equalsIgnoreCase(item.type()));
    }

    private void savePollOptions(Post post, List<String> options) {
        List<PollOption> pollOptions = options.stream()
            .map(optionText -> PollOption.builder()
                .post(post)
                .optionText(optionText)
                .voteCount(0)
                .build())
            .toList();
        
        pollOptionRepository.saveAll(pollOptions);
    }

    private void handleGroupAssociation(CreatePostRequest request, Post post) {
        if (request.groupId() != null) {
            Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new ApiException("Group not found"));
            post.setGroup(group);
        }
    }

    private void processPostTypeAndMedia(CreatePostRequest request, Post post, String existingType) {
        String postType = "text";
        
        if (request.poll() != null) {
            postType = "poll";
            post.setPollQuestion(request.poll().question());
        } else if (request.media() != null) {
            postType = hasVideo(request.media()) ? "video" : (request.media().isEmpty() ? "text" : "image");
            post.setMediaItems(mapMediaItemsFromRequest(request.media()));
        } else if (existingType != null && !existingType.isBlank()) {
            postType = existingType;
        }
        
        post.setType(postType);
    }

}
