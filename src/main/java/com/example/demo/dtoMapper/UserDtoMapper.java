package com.example.demo.dtoMapper;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;

public class UserDtoMapper {

    public static UserDto fromUser(User user) {
        return new UserDto(user);
    }

    public static User toUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setStaffId(userDto.getStaffId());
        user.setName(userDto.getName());
        user.setDob(userDto.getDob());
        user.setEmail(userDto.getEmail());
        user.setDepartment(userDto.getDepartment());
        user.setPhoto(userDto.getPhoto());
        user.setBiography(userDto.getBiography());
        user.setStatus(userDto.isStatus());
        user.setRole(userDto.getRole());
        return user;
    }

}
