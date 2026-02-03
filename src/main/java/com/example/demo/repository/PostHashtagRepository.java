package com.example.demo.repository;

import com.example.demo.entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, PostHashtag.PostHashtagId> {
    List<PostHashtag> findByPostId(Long postId);
    List<PostHashtag> findByHashtagId(Long hashtagId);
}
