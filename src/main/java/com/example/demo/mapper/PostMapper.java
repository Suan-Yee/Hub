package com.example.demo.mapper;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.PollOption;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import com.example.demo.enumeration.ReactionType;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.PollOptionRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.utils.TimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostMapper {

    private final ReactionRepository reactionRepository;
    private final PollOptionRepository pollOptionRepository;
    private final BookmarkRepository bookmarkRepository;

    public PostResponse mapToPostResponse(Post post, Long currentUserId) {
        User author = post.getUser();

        Map<ReactionType, Integer> reactionCounts = fetchReactionCounts(post.getId());

        ReactionType userReaction = fetchUserReaction(currentUserId, post.getId());
        
        boolean liked = userReaction == ReactionType.LIKE;
        boolean bookmarked = currentUserId != null && 
            bookmarkRepository.existsByUserIdAndPostId(currentUserId, post.getId());

        int totalLikes = reactionCounts.values().stream().mapToInt(Integer::intValue).sum();

        List<PostResponse.MediaItemResponse> mediaResponses = mapMediaItems(post.getMediaItems());

        PostResponse.PollResponse pollResponse = mapPoll(post);

        PostResponse resharedFromResponse = post.getOriginalPost() != null 
            ? mapToPostResponse(post.getOriginalPost(), currentUserId) 
            : null;

        return new PostResponse(
            post.getId().toString(),
            author.getUsername(),
            "@" + author.getUsername(),
            author.getAvatarUrl(),
            TimeFormatter.formatTimeAgo(post.getCreatedAt()),
            post.getContent(),
            Objects.requireNonNullElse(post.getTags(), List.of()),
            Objects.requireNonNullElse(post.getMentions(), List.of()),
            mediaResponses,
            pollResponse,
            List.of(),
            totalLikes,
            liked,
            bookmarked,
            post.getGroup() != null ? post.getGroup().getName() : null,
            Boolean.TRUE.equals(post.getEdited()),
            Boolean.TRUE.equals(post.getEdited()) 
                ? TimeFormatter.formatTimeAgo(post.getUpdatedAt()) 
                : null,
            resharedFromResponse,
            reactionCounts,
            userReaction
        );
    }

    private Map<ReactionType, Integer> fetchReactionCounts(Long postId) {
        Map<ReactionType, Integer> reactionCounts = new EnumMap<>(ReactionType.class);
        Arrays.stream(ReactionType.values()).forEach(type -> reactionCounts.put(type, 0));

        List<Object[]> grouped = reactionRepository.countReactionsGroupedByType("post", postId);
        grouped.forEach(row -> {
            String typeStr = (String) row[0];
            Number count = (Number) row[1];
            try {
                ReactionType type = ReactionType.valueOf(typeStr.toUpperCase());
                reactionCounts.put(type, count.intValue());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown reaction type: {}", typeStr);
            }
        });

        return reactionCounts;
    }

    private ReactionType fetchUserReaction(Long currentUserId, Long postId) {
        if (currentUserId == null) {
            return null;
        }

        return reactionRepository
            .findByUserIdAndTargetTypeAndTargetId(currentUserId, "post", postId)
            .map(Reaction::getReactionType)
            .map(typeStr -> {
                try {
                    return ReactionType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid reaction type: {}", typeStr);
                    return null;
                }
            })
            .orElse(null);
    }

    private List<PostResponse.MediaItemResponse> mapMediaItems(List<Post.MediaItem> mediaItems) {
        if (mediaItems == null || mediaItems.isEmpty()) {
            return List.of();
        }

        return mediaItems.stream()
            .map(item -> new PostResponse.MediaItemResponse(
                item.getId(),
                item.getType(),
                item.getUrl()
            ))
            .toList();
    }

    private PostResponse.PollResponse mapPoll(Post post) {
        if (post.getPollQuestion() == null) {
            return null;
        }

        List<PollOption> pollOptions = pollOptionRepository.findByPostId(post.getId());
        int totalVotes = pollOptions.stream().mapToInt(PollOption::getVoteCount).sum();
        
        List<PostResponse.PollOptionResponse> optionResponses = pollOptions.stream()
            .map(option -> new PostResponse.PollOptionResponse(
                option.getId().toString(),
                option.getOptionText(),
                option.getVoteCount(),
                false
            ))
            .toList();
        
        return new PostResponse.PollResponse(
            post.getPollQuestion(),
            optionResponses,
            totalVotes
        );
    }
}
