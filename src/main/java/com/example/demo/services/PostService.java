package com.example.demo.services;

import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    List<Post> findByUserName(String userName);

    List<Post> findByTopicName(String name);

    List<Post> findAllPosts();

    List<Post> findByGroupId(Long groupId);

    Long totalPostByUser(Long userId);

   List<Post> findBySpecification(String topicName, String userName);
}
