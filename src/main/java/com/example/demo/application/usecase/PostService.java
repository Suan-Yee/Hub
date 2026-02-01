package com.example.demo.application.usecase;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Content;
import com.example.demo.entity.Group;
import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.util.List;

public interface PostService {

    List<Post> findByUserName(String userName);

    List<Post> findByTopicName(String name);

    PostDto findByIdPost(Long postId,Long userId);

    Post findById(Long postId);

    PostDto findByPostDtoId(Long postId);

    Long postsCountByTopic(String topicName);

    List<Post> findAllPosts();

    List<Post> findByGroupId(Long groupId);

    Long totalPostByUser(Long userId);

    Page<PostDto> findBySpecification(String topicName, String userName, Long userId,int page, int size, Sort sort);

    Post createPost(Content content,String text);
    void deletePost(Long id);

//    List<Post> getPostFromGroups(Long userId);
    PostDto transformToPostDto(Post post, Long userId);

    Long totalTopicPost(Long topicId);

    List<PostDto> getAllPostByUser();

    Post saveGroup(Post post,Group group);

    List<PostDto> getPostFromGroup(Long groupId,Principal principal);
    List<PostDto> getTrendingPostsInOneWeek();
    List<PostDto> getTrendingPostsInOneMonth();

    List<PostDto> getTrendingPostsInOneYear();

    long getTotalPostsForCurrentDay();
    long getTotalPostsForCurrentMonth();
    long getTotalPostsForCurrentYear();

    List<PostDto> findTopPostsInGroup(Long groupId);

    List<PostDto> findByUserId(Long userId);
}
