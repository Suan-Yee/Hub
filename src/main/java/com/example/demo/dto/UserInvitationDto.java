package com.example.demo.dto;

import java.util.List;

public record UserInvitationDto(
        Long communityId,
        List<Long> userIds
) {
    public Long getCommunityId() {
        return communityId;
    }

    public List<Long> getUserIds() {
        return userIds;
    }
}
