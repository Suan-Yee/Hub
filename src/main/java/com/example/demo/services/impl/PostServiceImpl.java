package com.example.demo.services.impl;

import com.example.demo.entity.Post;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.services.PostService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;

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
    public List<Post> findBySpecification(String topicName, String userName) {
            Specification<Post> spec = (Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
                if (topicName!= null) {
                    Join<Post, Topic> topicJoin = root.join("topic", JoinType.INNER);
                    return builder.equal(topicJoin.get("name"), topicName);
                } else if (userName!= null) {
                    Join<Post, User> userJoin = root.join("user", JoinType.INNER);
                    return builder.equal(userJoin.get("name"), userName);
                } else {
                    return builder.conjunction();
                }
            };
            return postRepository.findAll(spec);
        }
}
