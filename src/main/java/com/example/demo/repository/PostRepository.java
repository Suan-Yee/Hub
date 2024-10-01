package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long>, JpaSpecificationExecutor<Post> {

    @Query("SELECT p FROM Post p WHERE p.user.name = :userName ORDER BY p.createdAt DESC")
    List<Post> findByUserName(@Param("userName") String userName);

    @Query("SELECT p FROM Post p WHERE p.topic.name = :topicName ORDER BY p.createdAt DESC")
    List<Post> findByTopicName(@Param("topicName") String topic);

    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllPosts();

    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId ORDER BY p.createdAt DESC ")
    List<Post> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    Long countByUser(@Param("userId") Long userId);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startDate AND p.createdAt < :endDate")
    List<Post> findByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.topic.name = :topicName")
    Long countByTopic(@Param("topicName")String topicName);


    @Query("SELECT COUNT(p) FROM Post p WHERE p.topic.id = :topicId")
    Long countByTopic(@Param("topicId") Long topicId);

    @Query("SELECT p FROM Post p WHERE p.status = false AND p.user.id = :userId ORDER BY p.createdAt DESC ")
    List<Post> findByUser(@Param("userId") Long userId);

    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId AND p.status = false")
    List<Post> getPostFromGroup(@Param("groupId") Long groupId);

    @Query("SELECT p FROM Post p LEFT JOIN p.likes l LEFT JOIN p.comments c WHERE p.group.id = :groupId AND p.status = false GROUP BY p.id ORDER BY (COUNT(l) + COUNT(c)) DESC")
    List<Post> findTopPostsByLikesAndCommentsInGroup(@Param("groupId") Long groupId);

    @Query(value = "SELECT p.*, (SELECT COUNT(*) FROM `Like` l WHERE l.post_id = p.id) + " +
            "(SELECT COUNT(*) FROM Comment c WHERE c.post_id = p.id) AS total_likes_comments " +
            "FROM Post p " +
            "WHERE p.created_at >= :oneWeekAgo " +  // Filter posts within the last week
            "ORDER BY total_likes_comments DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Post> findTop5TrendingPosts(LocalDateTime oneWeekAgo);

    @Query(value = "SELECT p.*, (SELECT COUNT(*) FROM `Like` l WHERE l.post_id = p.id) + " +
            "(SELECT COUNT(*) FROM Comment c WHERE c.post_id = p.id) AS total_likes_comments " +
            "FROM Post p " +
            "WHERE p.created_at >= :oneMonthAgo " +  // Filter posts within the last week
            "ORDER BY total_likes_comments DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Post> findTop5TrendingPostsInOneMonth(LocalDateTime oneMonthAgo);

    @Query(value = "SELECT p.*, (SELECT COUNT(*) FROM `Like` l WHERE l.post_id = p.id) + " +
            "(SELECT COUNT(*) FROM Comment c WHERE c.post_id = p.id) AS total_likes_comments " +
            "FROM Post p " +
            "WHERE p.created_at >= :oneYearAgo " +  // Filter posts within the last week
            "ORDER BY total_likes_comments DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Post> findTop5TrendingPostsInOneYear(LocalDateTime oneYearAgo);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countPostsFromDayOrMonthOrYear(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId AND p.group.id = :groupId")
    int countByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Query("SELECT p FROM Post p where p.user.id = :userId")
    List<Post> findPostByUserId(@Param("userId")Long userId);

}
