package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long>, JpaSpecificationExecutor<Post> {

    @Query("SELECT p FROM Post p WHERE p.user.name = :userName ORDER BY p.createdAt DESC")
    List<Post> findByUserName(@Param("userName") String userName);

    @Query("SELECT p FROM Post p WHERE p.topic.name = :topicName ORDER BY p.createdAt DESC")
    List<Post> findByTopicName(@Param("topicName") String topic);

    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllPosts();

    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId ORDER BY p.createdAt DESC ")
    List<Post> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    Long countByUser(@Param("userId") Long userId);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startDate AND p.createdAt < :endDate")
    List<Post> findByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
