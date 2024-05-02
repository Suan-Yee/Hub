package com.example.demo.dto;

import com.example.demo.entity.Group;
import lombok.*;



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
    private boolean active;
    private String ownerUserName;


    public GroupDto(Group group){
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.rule = group.getRule();
        this.image = group.getImage();
        this.active = group.isActive();
        if (group.getGroupOwner() != null) {
            this.ownerUserName = group.getGroupOwner().getName();
        } else {
            this.ownerUserName = "Unknown"; // or any default value you prefer
        }
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
        group.setActive(groupDto.isActive());

        return group;
    }
}
