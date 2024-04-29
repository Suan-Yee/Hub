package com.example.demo.repository;

import com.example.demo.entity.BookMark;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark,Long> {

    List<BookMark> findByUserAndStatus(User user,boolean status);

    @Query("SELECT b FROM  BookMark b WHERE b.post.id =:postId")
    Optional<BookMark> findByUserAndPost(@Param("postId")Long postId);
}
