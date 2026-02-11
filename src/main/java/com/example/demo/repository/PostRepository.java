package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
    List<Post> findByGroupId(Long groupId);
    List<Post> findByType(String type);
    Page<Post> findByUserId(Long userId, Pageable pageable);
    Page<Post> findByGroupId(Long groupId, Pageable pageable);
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Post p JOIN p.hashtags h WHERE h.tag = :hashtag")
    List<Post> findByHashtag(@Param("hashtag") String hashtag);
    
    @Query("SELECT p FROM Post p WHERE p.visibility = 'public' ORDER BY p.createdAt DESC")
    Page<Post> findPublicPosts(Pageable pageable);

    @Query("""
        SELECT p
        FROM Post p
        WHERE
            (
                p.visibility = 'public'
                OR p.user.id = :currentUserId
                OR (
                    p.visibility = 'followers'
                    AND EXISTS (
                        SELECT 1
                        FROM UserRelation ur
                        WHERE ur.follower.id = :currentUserId
                          AND ur.following.id = p.user.id
                          AND ur.status = 'accepted'
                    )
                )
            )
            AND NOT EXISTS (
                SELECT 1
                FROM UserBlock ub
                WHERE (ub.blocker.id = :currentUserId AND ub.blocked.id = p.user.id)
                   OR (ub.blocker.id = p.user.id AND ub.blocked.id = :currentUserId)
            )
        ORDER BY p.createdAt DESC
        """)
    Page<Post> findHomeFeed(@Param("currentUserId") Long currentUserId, Pageable pageable);
}
