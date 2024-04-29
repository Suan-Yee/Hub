package com.example.demo.services.impl;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.*;
import com.example.demo.repository.LikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.services.CommentService;
import com.example.demo.services.PostService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service @Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentService commentService;

    @Override
    public List<Post> findByUserName(String userName) {
        return postRepository.findByUserName(userName);
    }

    @Override
    public List<Post> findByTopicName(String topicName) {
        return postRepository.findByTopicName(topicName);
    }

    @Override
    public Post findById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }

    @Override
    public Long postsCountByTopic(String topicName) {
        return postRepository.countByTopic(topicName);
    }

    @Override
    public List<Post> findAllPosts() {
        return postRepository.findAllPosts();
    }

    @Override
    public List<Post> findByGroupId(Long groupId) {
        return postRepository.findByGroupId(groupId);
    }

    @Override
    public PostDto findByIdPost(Long postId,Long userId) {
        Post post = postRepository.findById(postId).orElse(null);
        PostDto postDto = transformToPostDto(post,userId);
        return postDto;
    }

    @Override
    public Long totalPostByUser(Long userId) {
        return postRepository.countByUser(userId);
    }

    @Override
    public Page<PostDto> findBySpecification(String topicName, String search, Long userId, int page, int size, Sort sort) {
        Specification<Post> spec = buildSpecification(topicName, search);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> posts = postRepository.findAll(spec, pageable);
        return posts.map(post -> transformToPostDto(post, userId));
    }

    private Specification<Post> buildSpecification(String topicName, String search) {
        return (Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (topicName != null) {
                Join<Post, Topic> topicJoin = root.join("topic", JoinType.INNER);
                predicates.add(builder.equal(topicJoin.get("name"), topicName));
            } else if (search != null) {
                String[] searchTerms = search.split("\\s+");
                for (String term : searchTerms) {
                    Join<Post, User> userJoin = root.join("user", JoinType.LEFT);
                    predicates.add(builder.like(userJoin.get("name"), "%" + term + "%"));
                    Join<Post, Content> contentJoin = root.join("content", JoinType.LEFT);
                    predicates.add(builder.like(contentJoin.get("text"), "%" + term + "%"));
                }
            }
            return predicates.isEmpty() ? builder.conjunction() : builder.or(predicates.toArray(new Predicate[0]));
        };
    }

    private PostDto transformToPostDto(Post post, Long userId) {
        PostDto postDto = new PostDto(post);
        postDto.setLikedByCurrentUser(isPostLikedByUser(post, userId));
        postDto.setLikes(likeRepository.totalLikePost(post.getId()).orElse(0L).intValue());
        postDto.setBookMark(isPostBookmarkedByUser(post, userId));
        postDto.setCommentCount(commentService.countCommentByPostId(post.getId()));
        return postDto;
    }
    private boolean isPostLikedByUser(Post post, Long userId) {
        return post.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(userId) && like.isLikeStatus());
    }

    private boolean isPostBookmarkedByUser(Post post, Long userId) {
        boolean result = post.getBookMarks().stream()
//                .peek(bookMark -> System.out.println("Checking bookmark for user: " + bookMark.getUser().getId() +
//                        ", post: " + bookMark.getPost().getId() +
//                        ", status: " + bookMark.isStatus()))
                .anyMatch(bookMark -> bookMark.getUser().getId().equals(userId) && bookMark.isStatus());
        return result;
    }
    @Override
    public Post createPost(Content content) {
        Post post = new Post();
        post.setContent(content);
        return postRepository.save(post);
    }



}
