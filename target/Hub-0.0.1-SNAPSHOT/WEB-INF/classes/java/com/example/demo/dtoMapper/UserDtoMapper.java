package com.example.demo.dtoMapper;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;
import org.springframework.beans.BeanUtils;

public class UserDtoMapper {

    public static UserDto fromUser(User user){
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user,userDto);
        return userDto;
    }

    public static User toUser(UserDto userDto){
        User user = new User();
        BeanUtils.copyProperties(userDto,user);
        return user;
    }

}
