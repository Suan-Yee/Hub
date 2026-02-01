package com.example.demo.application.usecase;

import com.example.demo.dto.LikeDto;
import com.example.demo.entity.CommentLike;

import java.security.Principal;

public interface CommentLikeService {

    boolean saveLikeForComment(LikeDto likeDto, Principal principal);
    void deleteByCommentId(Long commentId);

}
