package com.example.demo.dto;

public record InvitationDto(
        String groupName,
        String requestUserName,
        Long groupId,
        Long requestUserStaffId,
        String userProfile
) {
    public String getGroupName() {
        return groupName;
    }

    public String getRequestUserName() {
        return requestUserName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getRequestUserStaffId() {
        return requestUserStaffId;
    }

    public String getUserProfile() {
        return userProfile;
    }
}
