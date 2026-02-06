package com.example.demo.service;

import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface PostService {
    
    /**
     * Create a new post
     * @param request The post creation request
     * @param userId The ID of the user creating the post
     * @return The created post response
     * @throws IOException if media upload fails
     */
    PostResponse createPost(CreatePostRequest request, Long userId) throws IOException;
    
    /**
     * Get a post by ID
     * @param postId The post ID
     * @param currentUserId The ID of the current user (for liked/bookmarked status)
     * @return The post response
     */
    PostResponse getPostById(Long postId, Long currentUserId);
    
    /**
     * Get posts by user ID
     * @param userId The user ID
     * @param currentUserId The ID of the current user
     * @param pageable Pagination information
     * @return Page of posts
     */
    Page<PostResponse> getPostsByUserId(Long userId, Long currentUserId, Pageable pageable);
    
    /**
     * Get public feed posts
     * @param currentUserId The ID of the current user
     * @param pageable Pagination information
     * @return Page of posts
     */
    Page<PostResponse> getPublicFeed(Long currentUserId, Pageable pageable);
    
    /**
     * Get posts by group ID
     * @param groupId The group ID
     * @param currentUserId The ID of the current user
     * @param pageable Pagination information
     * @return Page of posts
     */
    Page<PostResponse> getPostsByGroupId(Long groupId, Long currentUserId, Pageable pageable);
    
    /**
     * Delete a post
     * @param postId The post ID
     * @param userId The ID of the user attempting to delete
     */
    void deletePost(Long postId, Long userId);
    
    /**
     * Update a post
     * @param postId The post ID
     * @param request The update request
     * @param userId The ID of the user attempting to update
     * @return The updated post response
     */
    PostResponse updatePost(Long postId, CreatePostRequest request, Long userId) throws IOException;
}
