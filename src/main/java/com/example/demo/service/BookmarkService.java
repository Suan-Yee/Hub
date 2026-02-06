package com.example.demo.service;

import com.example.demo.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookmarkService {
    
    /**
     * Add a bookmark to a post
     * @param postId The post ID
     * @param userId The user ID
     */
    void addBookmark(Long postId, Long userId);
    
    /**
     * Remove a bookmark from a post
     * @param postId The post ID
     * @param userId The user ID
     */
    void removeBookmark(Long postId, Long userId);
    
    /**
     * Get all bookmarked posts for a user
     * @param userId The user ID
     * @param currentUserId The current user ID (for liked/bookmarked status)
     * @param pageable Pagination information
     * @return Page of bookmarked posts
     */
    Page<PostResponse> getUserBookmarks(Long userId, Long currentUserId, Pageable pageable);
    
    /**
     * Check if a post is bookmarked by a user
     * @param postId The post ID
     * @param userId The user ID
     * @return true if bookmarked, false otherwise
     */
    boolean isBookmarked(Long postId, Long userId);
}
