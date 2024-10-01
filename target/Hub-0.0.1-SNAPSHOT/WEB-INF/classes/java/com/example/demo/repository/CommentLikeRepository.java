package com.example.demo.repository;

import com.example.demo.entity.CommentLike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike,Long> {

    @Query("SELECT l FROM CommentLike l WHERE l.comment.id = :commentId AND l.user.id = :userId")
    Optional<CommentLike> findLikeComment(@Param("commentId")Long commentId, @Param("userId")Long userId);

    void deleteByCommentId(Long commentId);
}
