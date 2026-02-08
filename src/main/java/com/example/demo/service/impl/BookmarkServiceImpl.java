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

    @Override
    public void addBookmark(Long postId, Long userId) {

    }

    @Override
    public void removeBookmark(Long postId, Long userId) {

    }

    @Override
    public Page<PostResponse> getUserBookmarks(Long userId, Long currentUserId, Pageable pageable) {
        return null;
    }

    @Override
    public boolean isBookmarked(Long postId, Long userId) {
        return false;
    }
}
