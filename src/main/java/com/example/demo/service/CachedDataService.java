package com.example.demo.service;

import com.example.demo.entity.Group;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for cached data access with Spring Cache
 * Uses Caffeine cache (configured in CacheConfig)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CachedDataService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;

    // ==================== User Cache ====================

    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public Optional<User> findUserById(Long id) {
        log.debug("Cache miss - Loading user from database: {}", id);
        return userRepository.findById(id);
    }

    @Cacheable(value = "users", key = "'username:' + #username", unless = "#result == null")
    public Optional<User> findUserByUsername(String username) {
        log.debug("Cache miss - Loading user by username from database: {}", username);
        return userRepository.findByUsername(username);
    }

    @Cacheable(value = "users", key = "'email:' + #email", unless = "#result == null")
    public Optional<User> findUserByEmail(String email) {
        log.debug("Cache miss - Loading user by email from database: {}", email);
        return userRepository.findByEmail(email);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void evictAllUsersCache() {
        log.debug("Evicting all users cache");
    }

    @CacheEvict(value = "users", key = "#id")
    public void evictUserCache(Long id) {
        log.debug("Evicting user cache for id: {}", id);
    }

    // ==================== Post Cache ====================

    @Cacheable(value = "posts", key = "#id", unless = "#result == null")
    public Optional<Post> findPostById(Long id) {
        log.debug("Cache miss - Loading post from database: {}", id);
        return postRepository.findById(id);
    }

    @Cacheable(value = "posts", key = "'user:' + #userId")
    public List<Post> findPostsByUserId(Long userId) {
        log.debug("Cache miss - Loading posts for user from database: {}", userId);
        return postRepository.findByUserId(userId);
    }

    @Cacheable(value = "posts", key = "'group:' + #groupId")
    public List<Post> findPostsByGroupId(Long groupId) {
        log.debug("Cache miss - Loading posts for group from database: {}", groupId);
        return postRepository.findByGroupId(groupId);
    }

    @CacheEvict(value = "posts", allEntries = true)
    public void evictAllPostsCache() {
        log.debug("Evicting all posts cache");
    }

    @CacheEvict(value = "posts", key = "#id")
    public void evictPostCache(Long id) {
        log.debug("Evicting post cache for id: {}", id);
    }

    // ==================== Group Cache ====================

    @Cacheable(value = "groups", key = "#id", unless = "#result == null")
    public Optional<Group> findGroupById(Long id) {
        log.debug("Cache miss - Loading group from database: {}", id);
        return groupRepository.findById(id);
    }

    @Cacheable(value = "groups", key = "'all'")
    public List<Group> findAllGroups() {
        log.debug("Cache miss - Loading all groups from database");
        return groupRepository.findAll();
    }

    @Cacheable(value = "groups", key = "'privacy:' + #privacyType")
    public List<Group> findGroupsByPrivacyType(String privacyType) {
        log.debug("Cache miss - Loading groups by privacy type from database: {}", privacyType);
        return groupRepository.findByPrivacyType(privacyType);
    }

    @CacheEvict(value = "groups", allEntries = true)
    public void evictAllGroupsCache() {
        log.debug("Evicting all groups cache");
    }

    @CacheEvict(value = "groups", key = "#id")
    public void evictGroupCache(Long id) {
        log.debug("Evicting group cache for id: {}", id);
    }

    // ==================== Cache Statistics ====================

    /**
     * Get cache statistics (requires metrics enabled)
     */
    public String getCacheStatistics() {
        return "Cache statistics available through /actuator/caches endpoint";
    }

    /**
     * Clear all caches
     */
    @CacheEvict(value = {"users", "posts", "groups"}, allEntries = true)
    public void clearAllCaches() {
        log.info("Clearing all caches");
    }

    // ==================== Helper Methods ====================

    /**
     * Warm up cache with frequently accessed data
     */
    public void warmUpCache() {
        log.info("Starting cache warm-up");
        
        // Load all groups (usually small dataset)
        findAllGroups();
        
        // Load public groups
        findGroupsByPrivacyType("public");
        
        log.info("Cache warm-up completed");
    }

    /**
     * Get cache health status
     */
    public boolean isCacheHealthy() {
        try {
            // Test cache operations
            findAllGroups();
            return true;
        } catch (Exception e) {
            log.error("Cache health check failed", e);
            return false;
        }
    }
}
