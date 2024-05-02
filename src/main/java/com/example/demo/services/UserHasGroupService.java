package com.example.demo.services;


import com.example.demo.entity.UserHasGroup;

import java.util.List;

public interface UserHasGroupService {

    public List<UserHasGroup> findByGroupId(Long id);


}
