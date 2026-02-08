package com.example.demo.dto.response;

import com.example.demo.enumeration.ReactionType;

import java.util.List;
import java.util.Map;

public record PostResponse(
    String id,
    String author,
    String handle,
    String avatar,
    String time,
    String content,
    List<String> tags,
    List<String> mentions,
    List<MediaItemResponse> media,
    PollResponse poll,
    List<CommentResponse> comments,
    int likes,
    boolean liked,
    boolean bookmarked,
    String group,
    boolean edited,
    String editedAt,
    PostResponse resharedFrom,
    Map<ReactionType, Integer> reactions,
    ReactionType userReaction
) {
    public record MediaItemResponse(
        String id,
        String type,
        String url
    ) {}
    
    public record PollResponse(
        String question,
        List<PollOptionResponse> options,
        int totalVotes
    ) {}
    
    public record PollOptionResponse(
        String id,
        String label,
        int votes,
        boolean voted
    ) {}
    
    public record CommentResponse(
        String id,
        String author,
        String handle,
        String avatar,
        String time,
        String text,
        List<CommentResponse> replies,
        int likes,
        boolean liked
    ) {}
}
