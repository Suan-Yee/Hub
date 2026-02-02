package com.example.demo.application.usecase;

import com.example.demo.dto.PostDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Group;
import com.example.demo.entity.Post;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import com.example.demo.infrastructure.persistence.repository.GroupRepository;
import com.example.demo.infrastructure.persistence.repository.PostRepository;
import com.example.demo.infrastructure.persistence.repository.TopicRepository;
import com.example.demo.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Centralized caching service for frequently accessed data.
 * 
 * Caching Strategy:
 * - Cache frequently read data
 * - Evict on updates
 * - Use appropriate TTL per data type
 * 
 * Benefits:
 * - Reduced database load
 * - Faster response times
 * - Better scalability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CachedDataService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TopicRepository topicRepository;
    private final GroupRepository groupRepository;
    private final OnlineStatusService onlineStatusService;

    /**
     * Get user by ID (cached).
     */
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        log.debug("Cache MISS: Loading user {}", userId);
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Get user by staff ID (cached).
     */
    @Cacheable(value = "users", key = "'staff-' + #staffId", unless = "#result == null")
    @Transactional(readOnly = true)
    public User getUserByStaffId(String staffId) {
        log.debug("Cache MISS: Loading user by staffId {}", staffId);
        return userRepository.findByStaffId(staffId).orElse(null);
    }

    /**
     * Get all users (cached).
     */
    @Cacheable(value = "users", key = "'all-users'")
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Cache MISS: Loading all users");
        return userRepository.findAll();
    }

    /**
     * Evict user cache when user is updated.
     */
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#user.id"),
            @CacheEvict(value = "users", key = "'staff-' + #user.staffId"),
            @CacheEvict(value = "users", key = "'all-users'"),
            @CacheEvict(value = "userProfile", key = "#user.id")
    })
    public void evictUserCache(User user) {
        log.debug("Evicted cache for user {}", user.getId());
    }

    /**
     * Get topic by ID (cached).
     */
    @Cacheable(value = "topics", key = "#topicId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Topic getTopicById(Long topicId) {
        log.debug("Cache MISS: Loading topic {}", topicId);
        return topicRepository.findById(topicId).orElse(null);
    }

    /**
     * Get all topics (cached - changes infrequently).
     */
    @Cacheable(value = "topics", key = "'all-topics'")
    @Transactional(readOnly = true)
    public List<Topic> getAllTopics() {
        log.debug("Cache MISS: Loading all topics");
        return topicRepository.findAll();
    }

    /**
     * Get trending posts (cached with short TTL).
     */
    @Cacheable(value = "trendingPosts", key = "'weekly'")
    @Transactional(readOnly = true)
    public List<Post> getTrendingPostsWeekly() {
        log.debug("Cache MISS: Loading weekly trending posts");
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return postRepository.findTop5TrendingPosts(oneWeekAgo);
    }

    /**
     * Get trending posts for the month (cached).
     */
    @Cacheable(value = "trendingPosts", key = "'monthly'")
    @Transactional(readOnly = true)
    public List<Post> getTrendingPostsMonthly() {
        log.debug("Cache MISS: Loading monthly trending posts");
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return postRepository.findTop5TrendingPostsInOneMonth(oneMonthAgo);
    }

    /**
     * Evict trending posts cache (call when new posts/likes/comments created).
     */
    @CacheEvict(value = "trendingPosts", allEntries = true)
    public void evictTrendingPostsCache() {
        log.debug("Evicted trending posts cache");
    }

    /**
     * Get group by ID (cached).
     */
    @Cacheable(value = "groups", key = "#groupId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Group getGroupById(Long groupId) {
        log.debug("Cache MISS: Loading group {}", groupId);
        return groupRepository.findById(groupId).orElse(null);
    }

    /**
     * Get top groups by posts (cached).
     */
    @Cacheable(value = "groups", key = "'top-by-posts'")
    @Transactional(readOnly = true)
    public List<Group> getTopGroupsByPosts() {
        log.debug("Cache MISS: Loading top groups by posts");
        return groupRepository.findTop5ByPostCount();
    }

    /**
     * Get top groups by members (cached).
     */
    @Cacheable(value = "groups", key = "'top-by-members'")
    @Transactional(readOnly = true)
    public List<Group> getTopGroupsByMembers() {
        log.debug("Cache MISS: Loading top groups by members");
        return groupRepository.findTop5ByMemberCount();
    }

    /**
     * Evict group cache.
     */
    @Caching(evict = {
            @CacheEvict(value = "groups", key = "#groupId"),
            @CacheEvict(value = "groups", key = "'top-by-posts'"),
            @CacheEvict(value = "groups", key = "'top-by-members'")
    })
    public void evictGroupCache(Long groupId) {
        log.debug("Evicted cache for group {}", groupId);
    }

    /**
     * Get online users (cached with very short TTL - 1 minute).
     */
    @Cacheable(value = "onlineUsers", key = "'current'")
    public Set<String> getOnlineUsers() {
        log.debug("Cache MISS: Loading online users");
        return onlineStatusService.getOnlineStaffIds();
    }

    /**
     * Evict online users cache (call when user connects/disconnects).
     */
    @CacheEvict(value = "onlineUsers", allEntries = true)
    public void evictOnlineUsersCache() {
        log.debug("Evicted online users cache");
    }

    /**
     * Clear all caches (admin function).
     */
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "posts", allEntries = true),
            @CacheEvict(value = "topics", allEntries = true),
            @CacheEvict(value = "groups", allEntries = true),
            @CacheEvict(value = "trendingPosts", allEntries = true),
            @CacheEvict(value = "onlineUsers", allEntries = true),
            @CacheEvict(value = "notifications", allEntries = true),
            @CacheEvict(value = "userProfile", allEntries = true)
    })
    public void clearAllCaches() {
        log.info("Cleared all application caches");
    }
}
