package com.example.demo.service.impl;

import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.*;
import com.example.demo.enumeration.ReactionType;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.*;
import com.example.demo.service.FileUploadService;
import com.example.demo.service.PostService;
import com.example.demo.utils.TimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PollOptionRepository pollOptionRepository;
    private final ReactionRepository reactionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final FileUploadService fileUploadService;

    @Override
    public PostResponse createPost(CreatePostRequest request, Long userId) throws IOException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException("User not found"));

        // Build post entity
        Post post = Post.builder()
            .user(user)
            .content(request.content())
            .tags(request.tags() != null ? request.tags() : new ArrayList<>())
            .mentions(request.mentions() != null ? request.mentions() : new ArrayList<>())
            .visibility(request.visibility() != null ? request.visibility() : "public")
            .likesCount(0)
            .commentsCount(0)
            .sharesCount(0)
            .edited(false)
            .build();

        // Handle group association
        if (request.groupId() != null) {
            Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new ApiException("Group not found"));
            post.setGroup(group);
        }

        // Determine post type
        String postType = "text";
        if (request.poll() != null) {
            postType = "poll";
            post.setPollQuestion(request.poll().question());
        } else if (request.mediaFiles() != null && !request.mediaFiles().isEmpty()) {
            postType = hasVideo(request.mediaFiles()) ? "video" : "image";
        }
        post.setType(postType);

        // Handle media uploads
        if (request.mediaFiles() != null && !request.mediaFiles().isEmpty()) {
            List<Post.MediaItem> mediaItems = uploadMediaFiles(request.mediaFiles());
            post.setMediaItems(mediaItems);
        }

        // Save post
        post = postRepository.save(post);

        // Handle poll options
        if (request.poll() != null) {
            createPollOptions(post, request.poll().options());
        }

        return mapToPostResponse(post, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));
        return mapToPostResponse(post, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUserId(Long userId, Long currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        return posts.map(post -> mapToPostResponse(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPublicFeed(Long currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findPublicPosts(pageable);
        return posts.map(post -> mapToPostResponse(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByGroupId(Long groupId, Long currentUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findByGroupId(groupId, pageable);
        return posts.map(post -> mapToPostResponse(post, currentUserId));
    }

    @Override
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new ApiException("You are not authorized to delete this post");
        }

        // Delete media files from Cloudinary
        if (post.getMediaItems() != null && !post.getMediaItems().isEmpty()) {
            for (Post.MediaItem mediaItem : post.getMediaItems()) {
                try {
                    fileUploadService.deleteImage(mediaItem.getUrl());
                } catch (IOException e) {
                    log.error("Failed to delete media file: {}", mediaItem.getUrl(), e);
                }
            }
        }

        postRepository.delete(post);
    }

    @Override
    public PostResponse updatePost(Long postId, CreatePostRequest request, Long userId) throws IOException {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new ApiException("You are not authorized to update this post");
        }

        // Update post fields
        post.setContent(request.content());
        post.setTags(request.tags() != null ? request.tags() : new ArrayList<>());
        post.setMentions(request.mentions() != null ? request.mentions() : new ArrayList<>());

        // Handle new media uploads
        if (request.mediaFiles() != null && !request.mediaFiles().isEmpty()) {
            // Delete old media
            if (post.getMediaItems() != null) {
                for (Post.MediaItem mediaItem : post.getMediaItems()) {
                    try {
                        fileUploadService.deleteImage(mediaItem.getUrl());
                    } catch (IOException e) {
                        log.error("Failed to delete old media: {}", mediaItem.getUrl(), e);
                    }
                }
            }
            
            // Upload new media
            List<Post.MediaItem> mediaItems = uploadMediaFiles(request.mediaFiles());
            post.setMediaItems(mediaItems);
        }

        post = postRepository.save(post);
        return mapToPostResponse(post, userId);
    }

    // Helper methods

    private List<Post.MediaItem> uploadMediaFiles(List<MultipartFile> files) throws IOException {
        List<Post.MediaItem> mediaItems = new ArrayList<>();
        
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            String url;
            String type;
            
            if (contentType != null && contentType.startsWith("video/")) {
                url = fileUploadService.uploadImage(file); // Cloudinary handles both
                type = "video";
            } else {
                url = fileUploadService.uploadImage(file);
                type = "image";
            }
            
            Post.MediaItem mediaItem = new Post.MediaItem(
                UUID.randomUUID().toString(),
                type,
                url
            );
            mediaItems.add(mediaItem);
        }
        
        return mediaItems;
    }

    private boolean hasVideo(List<MultipartFile> files) {
        return files.stream()
            .anyMatch(file -> {
                String contentType = file.getContentType();
                return contentType != null && contentType.startsWith("video/");
            });
    }

    private void createPollOptions(Post post, List<String> options) {
        for (String optionText : options) {
            PollOption pollOption = PollOption.builder()
                .post(post)
                .optionText(optionText)
                .voteCount(0)
                .build();
            pollOptionRepository.save(pollOption);
        }
    }

    public PostResponse mapToPostResponse(Post post, Long currentUserId) {
        User author = post.getUser();
        
        // Get reaction counts and user's reaction
        List<Reaction> reactions = reactionRepository.findByTargetTypeAndTargetId("post", post.getId());
        Map<ReactionType, Integer> reactionCounts = calculateReactionCounts(reactions);
        ReactionType userReaction = getUserReaction(reactions, currentUserId);
        
        // Check if user liked (for backward compatibility)
        boolean liked = userReaction == ReactionType.LIKE;
        
        // Check if user bookmarked
        boolean bookmarked = currentUserId != null && 
            bookmarkRepository.existsByUserIdAndPostId(currentUserId, post.getId());
        
        // Get total likes (sum of all reactions)
        int totalLikes = reactionCounts.values().stream().mapToInt(Integer::intValue).sum();
        
        // Map media items
        List<PostResponse.MediaItemResponse> mediaResponses = new ArrayList<>();
        if (post.getMediaItems() != null) {
            mediaResponses = post.getMediaItems().stream()
                .map(item -> new PostResponse.MediaItemResponse(
                    item.getId(),
                    item.getType(),
                    item.getUrl()
                ))
                .collect(Collectors.toList());
        }
        
        // Map poll
        PostResponse.PollResponse pollResponse = null;
        if (post.getPollQuestion() != null) {
            List<PollOption> pollOptions = pollOptionRepository.findByPostId(post.getId());
            int totalVotes = pollOptions.stream().mapToInt(PollOption::getVoteCount).sum();
            
            List<PostResponse.PollOptionResponse> optionResponses = pollOptions.stream()
                .map(option -> new PostResponse.PollOptionResponse(
                    option.getId().toString(),
                    option.getOptionText(),
                    option.getVoteCount(),
                    false // TODO: Check if current user voted for this option
                ))
                .collect(Collectors.toList());
            
            pollResponse = new PostResponse.PollResponse(
                post.getPollQuestion(),
                optionResponses,
                totalVotes
            );
        }
        
        // Map comments (top-level only for now)
        List<PostResponse.CommentResponse> commentResponses = new ArrayList<>();
        // TODO: Implement comment mapping if needed
        
        // Map reshared post
        PostResponse resharedFromResponse = null;
        if (post.getOriginalPost() != null) {
            resharedFromResponse = mapToPostResponse(post.getOriginalPost(), currentUserId);
        }
        
        return new PostResponse(
            post.getId().toString(),
            author.getUsername(),
            author.getId().toString(),
            "@" + author.getUsername(),
            author.getAvatarUrl(),
            TimeFormatter.formatTimeAgo(post.getCreatedAt()),
            post.getContent(),
            post.getTags() != null ? post.getTags() : new ArrayList<>(),
            post.getMentions() != null ? post.getMentions() : new ArrayList<>(),
            mediaResponses,
            pollResponse,
            commentResponses,
            totalLikes,
            liked,
            bookmarked,
            post.getGroup() != null ? post.getGroup().getName() : null,
            post.getEdited() != null && post.getEdited(),
            post.getEdited() != null && post.getEdited() ? 
                TimeFormatter.formatTimeAgo(post.getUpdatedAt()) : null,
            resharedFromResponse,
            reactionCounts,
            userReaction
        );
    }

    private Map<ReactionType, Integer> calculateReactionCounts(List<Reaction> reactions) {
        Map<ReactionType, Integer> counts = new EnumMap<>(ReactionType.class);
        
        // Initialize all reaction types with 0
        for (ReactionType type : ReactionType.values()) {
            counts.put(type, 0);
        }
        
        // Count reactions
        for (Reaction reaction : reactions) {
            try {
                ReactionType type = ReactionType.valueOf(reaction.getReactionType().toUpperCase());
                counts.put(type, counts.get(type) + 1);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown reaction type: {}", reaction.getReactionType());
            }
        }
        
        return counts;
    }

    private ReactionType getUserReaction(List<Reaction> reactions, Long userId) {
        if (userId == null) {
            return null;
        }
        
        return reactions.stream()
            .filter(r -> r.getUser().getId().equals(userId))
            .findFirst()
            .map(r -> {
                try {
                    return ReactionType.valueOf(r.getReactionType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .orElse(null);
    }
}
