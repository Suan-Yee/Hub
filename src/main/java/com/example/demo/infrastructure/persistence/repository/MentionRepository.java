package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.Mention;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MentionRepository extends JpaRepository<Mention, Long> {

    @Query("SELECT m FROM Mention m where m.post.id = :postId")
    List<Mention> getAllUserByMention(@Param("postId") Long postId);

    Mention findByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM Mention m WHERE m.post.id = :postId AND m.user.id = :userId")
    void deleteMentionUser(@Param("postId")Long postId,@Param("userId")Long uerId);
}
