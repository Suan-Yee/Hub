package com.example.demo.repository;

import com.example.demo.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Long> {

    @Query("SELECT l FROM Like l WHERE l.post.id = :postId AND l.user.id = :userId")
    Optional<Like> findLikePost(@Param("postId")Long postId, @Param("userId")Long userId);

    @Query("SELECT count(l) FROM Like l where l.post.id = :postId AND l.likeStatus = true ")
    Optional<Long> totalLikePost(@Param("postId")Long postId);
}
