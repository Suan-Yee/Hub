package com.example.demo.dto;

import com.example.demo.entity.Group;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDto {

    private Long id;
    private String name;
    private String description;
    private String rule;
    private String image;
    private Long ownerName;
    private String ownerUserName;
    private Long totalNumber;
    private int memberCount;
    private boolean deleted;
    private boolean isPrivate;
    private String visibility;

    public GroupDto(Group group){
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.rule = group.getRule();
        this.image = group.getImage();
        this.deleted = group.isDeleted();
        if (group.getGroupOwner() != null) {
            this.ownerUserName = group.getGroupOwner().getName();
        } else {
            this.ownerUserName = "Unknown";
        }
        if(group.getUserHasGroups() != null){
            this.memberCount = group.getUserHasGroups().size();
        }else{
            this.memberCount = 0;
        }
        this.isPrivate = group.isPrivate();
    }

    public GroupDto(Long id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public static Group toGroup(GroupDto groupDto){
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
