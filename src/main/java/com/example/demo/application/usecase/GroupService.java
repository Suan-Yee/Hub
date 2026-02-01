package com.example.demo.application.usecase;

import com.example.demo.dto.*;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserInvitation;
import com.example.demo.form.GroupForm;
import com.example.demo.form.GroupUserDisplay;
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

    List<GroupDto> findAllInactiveGroup();

    List<GroupDto> findGroupThatUserIsIn();

     Group getCommunityBy(Long id);

    void createGroup(GroupDto group,List<Long> id);

    List<String> getOwnerNamesByCommunityId(Long communityId);

     List<Group> getAllCommunityWithUserId();

    Optional<Group> findById(Long communityId);

    Group save(Group group);

    void deleteGroup(Long groupId);

     void kickGroup(Group group,List<Long> ids);

     GroupDto getCommunityById(Long communityId);

     Group getCommunityByIds(Long id);

     List<Long> findGroupIdsByUserId(Long userId);

     GroupDto findGroupsByIds(Long groupIds);

    Group findGroupsByRoomId(Long roomId);
    //sya
    List<Group> getGroupByUserId(Long userId);

    List<GroupForm> getAllGroupForm(Principal principal);

    List<GroupForm> randomGroupUserNotIn();

    GroupReport findTop5ByPostCount();
    GroupReport findTop5ByMemberCount();
    GroupPostPopulation getPostPopulationByGroup(Long groupId, int year);

    boolean userRequestToGroup(Long groupId);

    //sya
    List<GroupUserDisplay> getAllUserFromGroup(Long groupId);

    GroupUserDisplay getNewUser(Long groupId,Long userId);

    List<InvitationDto> getAllUserRequest(Principal principal,Long groupId);

    void kickUser(Long userId,Long groupId);

    void saveUserToGroup(Long userId,Long groupId);

    void toggleGroupStatus(Long groupId);
}
