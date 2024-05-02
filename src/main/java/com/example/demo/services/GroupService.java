package com.example.demo.services;

import com.example.demo.dto.GroupDto;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public interface GroupService {

     void createCommunity(GroupDto group, Principal principal);

     List<User> getAll();

     List<Group> getAllCommunity(Model model);

    List<GroupDto> findAllGroup();

     Group getCommunityBy(Long id);

    void createGroup(GroupDto group,List<Long> id);

    List<String> getOwnerNamesByCommunityId(Long communityId);

     List<Group> getAllCommunityWithUserId();

    Optional<Group> findById(Long communityId);

    Group save(Group group);

    void deleteGroup(Long groupId);

     void kickGroup(Group group,List<Long> ids);

     Group getCommunityById(Long communityId);

     List<Long> findGroupIdsByUserId(Long userId);

     GroupDto findGroupsByIds(Long groupIds);

    Group findGroupsByRoomId(Long roomId);
}
