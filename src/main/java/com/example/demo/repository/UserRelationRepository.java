package com.example.demo.repository;

import com.example.demo.entity.UserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRelationRepository extends JpaRepository<UserRelation, UserRelation.UserRelationId> {
    List<UserRelation> findByFollowerId(Long followerId);
    List<UserRelation> findByFollowingId(Long followingId);
    Optional<UserRelation> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    long countByFollowingId(Long followingId);
    long countByFollowerId(Long followerId);
}
