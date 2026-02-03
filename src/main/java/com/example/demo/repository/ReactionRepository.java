package com.example.demo.repository;

import com.example.demo.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findByTargetTypeAndTargetId(String targetType, Long targetId);
    Optional<Reaction> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
}
