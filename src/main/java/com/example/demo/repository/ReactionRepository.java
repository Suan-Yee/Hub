package com.example.demo.repository;

import com.example.demo.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findByTargetTypeAndTargetId(String targetType, Long targetId);
    Optional<Reaction> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    long countByTargetTypeAndTargetId(String targetType, Long targetId);
    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    @Query("SELECT r.reactionType, COUNT(r) FROM Reaction r WHERE r.targetType = :targetType AND r.targetId = :targetId GROUP BY r.reactionType")
    List<Object[]> countReactionsGroupedByType(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
