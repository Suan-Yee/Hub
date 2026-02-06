package com.example.demo.service.impl;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.Bookmark;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostServiceImpl postService;

    @Override
    public void addBookmark(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException("User not found"));

        // Check if bookmark already exists
        if (bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new ApiException("Post already bookmarked");
        }

        Bookmark.BookmarkId bookmarkId = new Bookmark.BookmarkId(userId, postId);
        Bookmark bookmark = Bookmark.builder()
            .id(bookmarkId)
            .user(user)
            .post(post)
            .build();

        bookmarkRepository.save(bookmark);
        log.info("User {} bookmarked post {}", userId, postId);
    }

    @Override
    public void removeBookmark(Long postId, Long userId) {
        Bookmark.BookmarkId bookmarkId = new Bookmark.BookmarkId(userId, postId);
        
        if (!bookmarkRepository.existsById(bookmarkId)) {
            throw new ApiException("Bookmark not found");
        }

        bookmarkRepository.deleteById(bookmarkId);
        log.info("User {} removed bookmark from post {}", userId, postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getUserBookmarks(Long userId, Long currentUserId, Pageable pageable) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
        
        List<PostResponse> postResponses = bookmarks.stream()
            .map(Bookmark::getPost)
            .map(post -> postService.mapToPostResponse(post, currentUserId))
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), postResponses.size());
        
        List<PostResponse> pageContent = postResponses.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, postResponses.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long postId, Long userId) {
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
    }
}
