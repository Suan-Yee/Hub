package com.example.demo.service;

import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface PostService {
    
    PostResponse createPost(CreatePostRequest request, Long userId);
    PostResponse getPostById(Long postId, Long currentUserId);
    Page<PostResponse> getPostsByUserId(Long userId, Long currentUserId, Pageable pageable);
    Page<PostResponse> getHomeFeed(Long currentUserId, Pageable pageable);
    Page<PostResponse> getPublicFeed(Long currentUserId, Pageable pageable);
    Page<PostResponse> getPostsByGroupId(Long groupId, Long currentUserId, Pageable pageable);
    void deletePost(Long postId, Long userId);
    PostResponse updatePost(Long postId, CreatePostRequest request, Long userId);
}
