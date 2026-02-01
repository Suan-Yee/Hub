package com.example.demo.application.usecase;

import com.example.demo.dto.LikeDto;
import com.example.demo.entity.Like;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import java.security.Principal;

public interface LikeService {

    boolean saveLike(LikeDto likeDto, Principal principal);
    Like createNewLike(User user, Post post);
    Long totalLikePost(Long postId);

}
