package com.example.demo.dto;

import com.example.demo.entity.*;
import com.example.demo.enumeration.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String staffId;
    private String name;
    private String dob;
    private String email;
    private String department;
    private String photo;
    private String biography;
    private boolean status;
    private Role role;

    @JsonIgnore
    private List<UserHasGroup> userHasGroup;

    public UserDto(User user){
        this.id = user.getId();
        this.staffId = user.getStaffId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.department = user.getDepartment();
        this.photo = user.getPhoto();
        this.role = user.getRole();
        this.userHasGroup = user.getGroups();

    }

    public UserDto(String staffId, String name, String department,  Role role, String photo, Long id) {
        this.staffId = staffId;
        this.name = name;
        this.department = department;
        this.role = role;
        this.photo=photo;
        this.id=id;
    }
}
