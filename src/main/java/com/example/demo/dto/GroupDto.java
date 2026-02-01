package com.example.demo.dto;

import com.example.demo.entity.Group;
import lombok.Builder;

@Builder
public record GroupDto(
        Long id,
        String name,
        String description,
        String rule,
        String image,
        Long ownerName,
        String ownerUserName,
        Long totalNumber,
        int memberCount,
        boolean deleted,
        boolean isPrivate,
        String visibility
) {
    public GroupDto(Group group) {
        this(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getRule(),
                group.getImage(),
                null,
                group.getGroupOwner() != null ? group.getGroupOwner().getName() : "Unknown",
                null,
                group.getUserHasGroups() != null ? group.getUserHasGroups().size() : 0,
                group.isDeleted(),
                group.isPrivate(),
                null
        );
    }

    public GroupDto(Long id, String name, String image) {
        this(id, name, null, null, image, null, null, null, 0, false, false, null);
    }

    public GroupDto withTotalNumber(Long value) {
        return new GroupDto(id, name, description, rule, image, ownerName, ownerUserName, value, memberCount,
                deleted, isPrivate, visibility);
    }

    public GroupDto withDeleted(boolean value) {
        return new GroupDto(id, name, description, rule, image, ownerName, ownerUserName, totalNumber, memberCount,
                value, isPrivate, visibility);
    }

    public GroupDto withPrivate(boolean value) {
        return new GroupDto(id, name, description, rule, image, ownerName, ownerUserName, totalNumber, memberCount,
                deleted, value, visibility);
    }

    public GroupDto withImage(String value) {
        return new GroupDto(id, name, description, rule, value, ownerName, ownerUserName, totalNumber, memberCount,
                deleted, isPrivate, visibility);
    }

    public GroupDto withVisibility(String value) {
        return new GroupDto(id, name, description, rule, image, ownerName, ownerUserName, totalNumber, memberCount,
                deleted, isPrivate, value);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRule() {
        return rule;
    }

    public String getImage() {
        return image;
    }

    public Long getOwnerName() {
        return ownerName;
    }

    public String getOwnerUserName() {
        return ownerUserName;
    }

    public Long getTotalNumber() {
        return totalNumber;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getVisibility() {
        return visibility;
    }

    public static Group toGroup(GroupDto groupDto) {
        Group group = new Group();
        group.setName(groupDto.getName());
        group.setDescription(groupDto.getDescription());
        group.setRule(groupDto.getRule());
        group.setImage(groupDto.getImage());
        group.setDeleted(groupDto.isDeleted());
        group.setPrivate(groupDto.isPrivate());
        return group;
    }
}
