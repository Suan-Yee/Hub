package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.PostTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PostTopicRepository extends JpaRepository<PostTopic,Long> {

    @Query("SELECT pt.topic.name FROM PostTopic pt WHERE pt.post.id = :postId")
    List<String> topicName(@Param("postId") Long postId);

    @Query("SELECT pt from PostTopic pt WHERE pt.post.id = :postId AND pt.topic.id = :topicId")
    Optional<PostTopic> findByTopicIdAndPostId(@Param("postId")Long postId, @Param("topicId")Long topicId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PostTopic pt WHERE pt.topic.name = :topicName AND pt.post.id = :postId")
    void deleteTopicFromPost(@Param("topicName")String topicName,Long postId);


}
