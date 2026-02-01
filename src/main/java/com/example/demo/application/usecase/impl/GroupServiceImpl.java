package com.example.demo.application.usecase.impl;


import com.example.demo.dto.*;
import com.example.demo.dtoMapper.GroupPostPopulationMapper;
import com.example.demo.dtoMapper.GroupReportMapper;
import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserHasGroup;
import com.example.demo.entity.UserInvitation;
import com.example.demo.exception.CommunityHubException;
import com.example.demo.form.GroupForm;
import com.example.demo.form.GroupUserDisplay;
import com.example.demo.infrastructure.persistence.repository.*;
import com.example.demo.application.usecase.GroupService;
import com.example.demo.application.usecase.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.demo.dto.GroupDto.toGroup;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserHasGroupRepository userHasGroupRepository;
    private final GroupReportMapper groupReportMapper;
    private final UserService userService;
    private final GroupPostPopulationMapper groupPostPopulationMapper;
    private final UserInvitationRepository userInvitationRepository;
    private final PostRepository postRepository;


    @Override
    public void createCommunity(GroupDto groupDto, Principal principal) {

        var user = userRepository.findByStaffId(principal.getName()).orElseThrow();

        GroupDto resultGroupDto = createGroupDto(groupDto, principal).withDeleted(false);
        Group group = toGroup(resultGroupDto);
        group.setGroupOwner(user);

        var com = groupRepository.save(group);

        UserHasGroup userHasGroup = new UserHasGroup();
        userHasGroup.setUser(user);
        userHasGroup.setGroup(com);
        userHasGroupRepository.save(userHasGroup);

    }
    private GroupDto createGroupDto(GroupDto groupDto,Principal principal){

        return GroupDto.builder().name(groupDto.getName()).
                description(groupDto.getDescription()).rule(groupDto.getRule())
                .image(groupDto.getImage()).deleted(true).isPrivate(groupDto.isPrivate()).build();

    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public List<Group> getAllCommunity(Model model) {
        List<Group> communities = groupRepository.findAll();

        return communities;
    }

    @Override
    public List<GroupDto> findAllGroup() {
        List<Group> groupList = groupRepository.findAllGroup();
        log.info("Group list {}",groupList);
        List<GroupDto> groupDtoList = groupList.stream().map(GroupDto::new).toList();
        groupDtoList = groupDtoList.stream()
                .map(groupDto -> groupDto.withTotalNumber(userHasGroupRepository.totalMembers(groupDto.getId())))
                .toList();
        return groupDtoList;
    }

    @Override
    public List<GroupDto> findAllInactiveGroup() {
        return groupRepository.findAllByDeletedIsTrue().stream().map(GroupDto::new).toList();
    }

    @Override
    public List<GroupDto> findGroupThatUserIsIn() {
        String staff = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staff);
        List<Group> groupList = groupRepository.findGroupsByUserId(user.getId());
        List<GroupDto> groupDtoList = groupList.stream().map(GroupDto::new).toList();
        return groupDtoList.stream()
                .map(groupDto -> groupDto.withTotalNumber(userHasGroupRepository.totalMembers(groupDto.getId())))
                .toList();
    }

    @Override
    public Group getCommunityBy(Long id) {
        return groupRepository.findById(id).orElseThrow();
    }

    @Override
    public void createGroup(GroupDto group, List<Long> ids) {
        Group group1 = groupRepository.findById(group.getId()).orElse(null);
        group1.setDescription(group.getDescription());
        group1.setRule(group.getRule());

        groupRepository.save(group1);

        for(Long u_id : ids){
            UserHasGroup user_group = new UserHasGroup();
            User user = userRepository.findById(u_id).orElseThrow();
            user_group.setGroup(group1);
            user_group.setUser(user);
            userHasGroupRepository.save(user_group);
        }
    }

    @Override
    public List<String> getOwnerNamesByCommunityId(Long communityId) {
        List<User> userList = userHasGroupRepository.findByGroupId(communityId);
        return userList.stream().map(User::getName).collect(Collectors.toList());
    }

    @Override
    public List<Group> getAllCommunityWithUserId() {
        var list = new ArrayList<UserHasGroup>();
        var user = userRepository.findByStaffId(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new CommunityHubException("not found"));
        var ids =    userHasGroupRepository.findDistinctGroupIdByUserId(user.getId());
        var communities = new ArrayList<Group>();
        for(var id : ids){
            communities.add(groupRepository.findById(id).orElseThrow(()->new CommunityHubException("not found group")));
        }
        return  communities;
    }

    @Override
    public Optional<Group> findById(Long communityId) {
        return groupRepository.findById(communityId);
    }

    @Override
    public Group save(Group group) {
        return groupRepository.save(group);
    }

    @Override
    public void deleteGroup(Long groupId) {
        groupRepository.deleteById(groupId);
    }

//    @Override
//    public void kickGroup(Group group, List<Long> ids) {
//        groupRepository.findById(group.getId()).ifPresent(c -> {
//
//            for (Long u_id : ids) {
//                userHasGroupRepository.deleteByCommunityIdAndUserId(group.getId(), u_id);
//            }
//
//        });
//    }

    @Override
    public void kickGroup(Group group, List<Long> userIds) {
        groupRepository.findById(group.getId()).ifPresent(g -> {
            userIds.forEach(userId -> {
                userHasGroupRepository.deleteByCommunityIdAndUserId(group.getId(), userId);
            });
        });
    }

    @Override
    public Group getCommunityByIds(Long communityId) {
        if (communityId == null) {
            throw new IllegalArgumentException("Community ID must not be null");
        }

        return groupRepository.findById(communityId).orElse(null);
    }

    @Override
    public GroupDto getCommunityById(Long communityId) {
        Group group = groupRepository.findById(communityId).orElse(null);
        GroupDto groupDto = new GroupDto(group);

        return groupDto;
    }

    @Override
    public List<Long> findGroupIdsByUserId(Long userId) {
       /* User userOptional = userRepository.findById(userId).orElse(null);
        if (userOptional != null) {
            User user = userOptional.get();
            return user.getGroups().stream().map(UserHasGroup::getId).collect(Collectors.toList());
        } else {
            // Handle case when user is not found
            return Collections.emptyList();
        }*/
       List<Long> groupIds = userGroup(userId);
       return groupIds;
    }
    private List<Long> userGroup(Long userId){
        List<Long> groupId = userHasGroupRepository.groupIdByUserId(userId);
        return groupId;
    }

    @Override
    public GroupDto findGroupsByIds(Long groupIds) {
        Group group = groupRepository.findById(groupIds).orElse(null);
        log.info("Group ID 1 {}",group);
        GroupDto groupDto = new GroupDto(group);
       /* groupDto.setId(group.getId());
        groupDto.setImage(group.getImage());
        groupDto.setName(group.getName());*/
//        groupDto.setTotalNumber((long) group.getUser().size());

        return groupDto;
    }

    @Override
    public Group findGroupsByRoomId(Long roomId) {
        return groupRepository.findById(roomId).orElse(null);
    }

    @Override
    public List<Group> getGroupByUserId(Long userId) {
        return userHasGroupRepository.findGroupByUserId(userId);
    }

    //sya
    @Override
    public List<GroupForm> getAllGroupForm(Principal principal){
        User user = userService.findAuthenticatedUser(principal);
        List<Group> groupList = groupRepository.findGroupsByUserId(user.getId());
        return createGroupForm(groupList,3);
    }
    @Override
    public List<GroupForm> randomGroupUserNotIn() {
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByStaffId(staffId).orElse(null);
        List<Group> randomGroup = groupRepository.getRandomGroupsNotInUser(user.getId());
        return createGroupForm(randomGroup,5);
    }
    private List<GroupForm> createGroupForm(List<Group> groupList,int limit){
        return groupList.stream().map(group -> {
            GroupForm groupForm = new GroupForm();
            groupForm.setId(group.getId());
            groupForm.setGroupName(group.getName());
            groupForm.setGroupPhoto(group.getImage());
            if(group.getUserHasGroups()!=null){
                groupForm.setTotalMember(group.getUserHasGroups().size());
            }
            if(group.getPosts().size() > 0){
                groupForm.setTotalPost(group.getPosts().size());
            }else{
                groupForm.setTotalPost(0);
            }
            groupForm.setUserImages(getUserImageList(group,limit));
            return groupForm;
        }).toList();
    }
    private List<String> getUserImageList(Group group,int limit){
        List<User> getUsers = userHasGroupRepository.findByGroupId(group.getId());
        Collections.shuffle(getUsers);

        return getUsers.stream()
                .limit(limit)
                .map(User::getPhoto).toList();
    }

    @Override
    public GroupReport findTop5ByPostCount() {
        List<Group> groups = groupRepository.findTop5ByPostCount();
        return  groupReportMapper.mapTogroupReport(groups);
    }

    @Override
    public GroupReport findTop5ByMemberCount() {
        List<Group> groups = groupRepository.findTop5ByMemberCount();
        return groupReportMapper.mapTogroupReport(groups);
    }

    @Override
    public GroupPostPopulation getPostPopulationByGroup(Long groupId, int year) {
        Group group = groupRepository.findById(groupId).orElse(null);
        return groupPostPopulationMapper.mapToGroupPostPopulation(group,year);
    }

    @Override
    public boolean userRequestToGroup(Long groupId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        UserInvitation userInvitation = new UserInvitation();
        userInvitation.setGroup(group);
        userInvitation.setJoined(false);
        userInvitation.setRequestId(user.getId());
        userInvitation.setRecipientId(group.getGroupOwner().getId());
        UserInvitation newUserRequest = userInvitationRepository.save(userInvitation);
        return true;
    }
    //sya
    @Override
    public List<GroupUserDisplay> getAllUserFromGroup(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
        Long groupOwnerId = group.getGroupOwner().getId();
        List<UserHasGroup> userHasGroupList = userHasGroupRepository.findByGroupIdAndUserIdNot(groupId, groupOwnerId);

        return userHasGroupList.stream()
                .map(userHasGroup -> {
                    int postCount = postRepository.countByUserIdAndGroupId(userHasGroup.getUser().getId(), groupId);
                    return new GroupUserDisplay(userHasGroup, postCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public GroupUserDisplay getNewUser(Long groupId, Long userId) {
       UserHasGroup userHasGroup = userHasGroupRepository.findChangeUser(groupId,userId);
       GroupUserDisplay newGroupUser = new GroupUserDisplay(userHasGroup,0);
        return newGroupUser;
    }

    @Override
    public List<InvitationDto> getAllUserRequest(Principal principal,Long groupId) {
        String staffId = principal.getName();
        User user = userService.findByStaffId(staffId);
        List<UserInvitation> allInvitaion = userInvitationRepository.getAllUserRequest(user.getId(),groupId);
        return allInvitaion.stream().map(invite -> {
            User requestUser = userService.findById(invite.getRequestId());
            return new InvitationDto(
                    invite.getGroup().getName(),
                    requestUser.getName(),
                    invite.getGroup().getId(),
                    invite.getRequestId(),
                    requestUser.getPhoto()
            );
        }).toList();
    }

    @Override
    @Transactional
    public void kickUser(Long userId, Long groupId) {
        userHasGroupRepository.deleteUserFromGroup(userId,groupId);
    }

    @Override
    public void saveUserToGroup(Long userId, Long groupId) {
        Group group = findById(groupId).orElse(null);
        User user = userService.findById(userId);
        UserHasGroup userHasGroup = new UserHasGroup();
        userHasGroup.setGroup(group);
        userHasGroup.setUser(user);
        userHasGroupRepository.save(userHasGroup);
    }

    @Transactional
    @Override
    public void toggleGroupStatus(Long groupId) {
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isPresent()) {
            Group group = optionalGroup.get();
            group.setDeleted(!group.isDeleted());
            groupRepository.save(group);
        } else {
            throw new EntityNotFoundException("Group not found with id: " + groupId);
        }
    }
}



