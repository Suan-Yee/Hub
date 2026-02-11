package com.example.demo.mapper;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.*;
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
        if (post == null) {
            return null;
        }

        Map<ReactionType, Integer> reactions = mapReactions("post", post.getId());
        ReactionType userReaction = getUserReaction("post", post.getId(), currentUserId);
        boolean reacted = userReaction != null;
        int totalReactions = reactions.values().stream().mapToInt(Integer::intValue).sum();
        boolean bookmarked = currentUserId != null
                && bookmarkRepository.existsByUserIdAndPostId(currentUserId, post.getId());

        return PostResponse
                .builder()
                .id(post.getId().toString())
                .author(post.getUser().getUsername())
                .handle("@" + post.getUser().getUsername())
                .avatar(post.getUser().getAvatarUrl())
                .time(formatTime(post))
                .content(post.getContent())
                .tags(mapTags(post.getTags()))
                .mentions(mapMentions(post.getMentions()))
                .media(mapToMediaItemResponse(post.getMediaItems()))
                .poll(mapToPollResponse(post, currentUserId))
                .comments(mapToCommentResponse(post.getComments(), currentUserId))
                .totalReactions(totalReactions)
                .reacted(reacted)
                .bookmarked(bookmarked)
                .group(post.getGroup() != null ? post.getGroup().getName() : null)
                .edited(Boolean.TRUE.equals(post.getEdited()))
                .editedAt(Boolean.TRUE.equals(post.getEdited()) && post.getUpdatedAt() != null
                        ? post.getUpdatedAt().toString()
                        : null)
                .reactions(reactions)
                .userReaction(userReaction)
                .build();
    }

    private List<PostResponse.MediaItemResponse> mapToMediaItemResponse(List<PostMediaItem> mediaItems) {
        if (mediaItems == null || mediaItems.isEmpty()) {
            return List.of();
        }
        return mediaItems.stream()
                .map(media ->
                        new PostResponse.MediaItemResponse(media.getMediaId(), media.getType(), media.getUrl()))
                .toList();
    }

    private List<String> mapTags(List<PostTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(PostTag::getTag)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> mapMentions(List<PostMention> mentions) {
        if (mentions == null || mentions.isEmpty()) {
            return List.of();
        }
        return mentions.stream()
                .map(PostMention::getUsername)
                .filter(Objects::nonNull)
                .toList();
    }

    private PostResponse.PollResponse mapToPollResponse(Post post, Long currentUserId) {
        if (!Objects.equals(post.getType(), "poll")) {
            return null;
        }

        List<PollOption> options = pollOptionRepository.findByPostId(post.getId());
        int totalVotes = options.stream()
                .mapToInt(option -> option.getVoters() != null ? option.getVoters().size() : 0)
                .sum();

        List<PostResponse.PollOptionResponse> optionResponses = options.stream()
                .map(option -> new PostResponse.PollOptionResponse(
                        option.getId().toString(),
                        option.getOptionText(),
                        option.getVoters() != null ? option.getVoters().size() : 0,
                        hasVoted(option, currentUserId)
                ))
                .toList();

        return new PostResponse.PollResponse(
                post.getPollQuestion(),
                optionResponses,
                totalVotes
        );
    }

    private boolean hasVoted(PollOption option, Long currentUserId) {
        if (currentUserId == null || option.getVoters() == null || option.getVoters().isEmpty()) {
            return false;
        }
        return option.getVoters().stream()
                .anyMatch(user -> user != null && Objects.equals(user.getId(), currentUserId));
    }

    private List<PostResponse.CommentResponse> mapToCommentResponse(Set<Comment> comments, Long currentUserId) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(comment -> mapSingleCommentResponse(comment, currentUserId))
                .toList();
    }

    private PostResponse.CommentResponse mapSingleCommentResponse(Comment comment, Long currentUserId) {
        Map<ReactionType, Integer> reactions = mapReactions("comment", comment.getId());
        ReactionType userReaction = getUserReaction("comment", comment.getId(), currentUserId);
        boolean liked = userReaction == ReactionType.LIKE;
        boolean reacted = userReaction != null;
        int likes = reactions.getOrDefault(ReactionType.LIKE, 0);
        int totalReactions = reactions.values().stream().mapToInt(Integer::intValue).sum();

        return new PostResponse.CommentResponse(
                comment.getId().toString(),
                comment.getUser().getUsername(),
                "@" + comment.getUser().getUsername(),
                comment.getUser().getAvatarUrl(),
                TimeFormatter.formatTimeAgo(comment.getCreatedAt()),
                comment.getContent(),
                mapReplies(comment.getReplies(), currentUserId),
                likes,
                liked,
                totalReactions,
                reacted,
                reactions,
                userReaction
        );
    }

    private List<PostResponse.CommentResponse> mapReplies(Set<Comment> replies, Long currentUserId) {
        if (replies == null || replies.isEmpty()) {
            return List.of();
        }

        return replies.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(reply -> mapSingleCommentResponse(reply, currentUserId))
                .toList();
    }

    private Map<ReactionType, Integer> mapReactions(String targetType, Long targetId) {
        List<Object[]> counts = reactionRepository.countReactionsGroupedByType(targetType, targetId);
        Map<ReactionType, Integer> reactionCounts = new EnumMap<>(ReactionType.class);
        for (Object[] row : counts) {
            if (row == null || row.length < 2) {
                continue;
            }
            String type = String.valueOf(row[0]);
            ReactionType reactionType = parseReactionType(type);
            if (reactionType == null) {
                continue;
            }
            int count = ((Number) row[1]).intValue();
            reactionCounts.put(reactionType, count);
        }
        return reactionCounts;
    }

    private ReactionType getUserReaction(String targetType, Long targetId, Long currentUserId) {
        if (currentUserId == null) {
            return null;
        }
        return reactionRepository.findByUserIdAndTargetTypeAndTargetId(currentUserId, targetType, targetId)
                .map(reaction -> parseReactionType(reaction.getReactionType()))
                .orElse(null);
    }

    private ReactionType parseReactionType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return ReactionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.debug("Unknown reaction type: {}", type);
            return null;
        }
    }

    private String formatTime(Post post) {
        if (post.getUpdatedAt() != null) {
            return TimeFormatter.formatTimeAgo(post.getUpdatedAt());
        }
        if (post.getCreatedAt() != null) {
            return TimeFormatter.formatTimeAgo(post.getCreatedAt());
        }
        return null;
    }
}
