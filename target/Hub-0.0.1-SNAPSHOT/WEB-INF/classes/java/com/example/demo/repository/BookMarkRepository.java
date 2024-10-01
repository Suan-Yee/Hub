package com.example.demo.repository;

import com.example.demo.entity.BookMark;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark,Long> {

    @Query("SELECT b FROM  BookMark b WHERE b.user.id = :userId AND b.status = :status  AND b.post.status = false")
    List<BookMark> findByUserAndStatus(@Param("userId")Long userId,@Param("status") boolean status);

    @Query("SELECT b FROM  BookMark b WHERE b.post.id =:postId")
    Optional<BookMark> findByUserAndPost(@Param("postId")Long postId);

    @Query("SELECT COUNT(b.post) FROM BookMark b WHERE b.post.topic.id = :topicId AND b.user.id = :userId AND b.status = true")
    Long countByBookMarkTopic(@Param("topicId")Long topicId,@Param("userId")Long userId);
}
