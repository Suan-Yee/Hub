package com.example.demo.services.impl;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Content;
import com.example.demo.entity.Post;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.services.PostService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

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
    public Long totalPostByUser(Long userId) {
        return postRepository.countByUser(userId);
    }

    @Override
    public List<PostDto> findBySpecification(String topicName, String search) {
        Specification<Post> spec = (Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (topicName != null) {
                Join<Post, Topic> topicJoin = root.join("topic", JoinType.INNER);
                predicates.add(builder.equal(topicJoin.get("name"), topicName));
            }else if (search != null) {
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
        List<Post> posts = postRepository.findAll(spec);
        return posts.stream().map(PostDto::new).collect(Collectors.toList());
    }
}
