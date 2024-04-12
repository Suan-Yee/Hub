package com.example.demo.services;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;

import java.util.List;

public interface PostService {

    List<Post> findByUserName(String userName);

    List<Post> findByTopicName(String name);

    Post findById(Long postId);

    Long postsCountByTopic(String topicName);

    List<Post> findAllPosts();

    List<Post> findByGroupId(Long groupId);

    Long totalPostByUser(Long userId);

   List<PostDto> findBySpecification(String topicName, String userName);
}
