package com.example.demo.services;

import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRequestGroup;

import java.util.List;

public interface UserRequestGroupService {

    public void createUserRequestGroup(User user, Group group);

    public List<UserRequestGroup> getRequestGroupsByUser(Long id);

    public void deleteUserRequestGroup(Long id);

    public UserRequestGroup getById(Long id);

    public void updateHasConfirmed(UserRequestGroup userRequestGroup);

    public List<UserRequestGroup> getAllRequestGroups();
}
