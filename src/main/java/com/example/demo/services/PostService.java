package com.example.demo.services;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Content;
import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface PostService {

    List<Post> findByUserName(String userName);

    List<Post> findByTopicName(String name);

    PostDto findByIdPost(Long postId,Long userId);

    Post findById(Long postId);

    Long postsCountByTopic(String topicName);

    List<Post> findAllPosts();

    List<Post> findByGroupId(Long groupId);

    Long totalPostByUser(Long userId);

    Page<PostDto> findBySpecification(String topicName, String userName, Long userId,int page, int size, Sort sort);

    Post createPost(Content content);


}
