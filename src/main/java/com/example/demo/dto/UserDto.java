package com.example.demo.dto;

import com.example.demo.entity.*;
import com.example.demo.enumeration.Role;
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
    private boolean status;
    private Role role;

    public UserDto(User user){
        this.id = user.getId();
        this.staffId = user.getStaffId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.department = user.getDepartment();
        this.photo = user.getPhoto();
        this.role = user.getRole();

    }
}
