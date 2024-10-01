package com.example.demo.repository;


import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserHasGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserHasGroupRepository extends JpaRepository<UserHasGroup,Long>{

//    List<UserHasGroup> findByGroupId(Long communityId);
//
//    @Query(value = "select distinct group _id from user_has_group where user_id = :userId",nativeQuery = true)
//    List<Long> findDistinctGroupIdByUserId(Long userId);
//
//
//    List<UserHasGroup> findByGroup(Group group);


    @Query("SELECT g.user FROM UserHasGroup g where g.group.id = :groupId")
    List<User> findByGroupId(@Param("groupId") Long groupId);

    @Query (value = "select distinct group _id from user_has_group where user_id = :userId",nativeQuery = true)
    List<Long> findDistinctGroupIdByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserHasGroup ug WHERE ug.group.id = :groupId AND ug.user.id = :userId")
    void deleteByCommunityIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);


    @Query("SELECT g.group.id FROM UserHasGroup g where g.user.id = :userId ")
    List<Long> groupIdByUserId(@Param("userId")Long userId);

    @Query("select g.group FROM UserHasGroup g WHERE g.user.id = :userId")
    List<Group> findGroupByUserId(@Param("userId")Long userId);

    @Query("SELECT g.user from UserHasGroup g where g.group.id = :groupId")
    List<User> findUsersByGroupId(@Param("groupId")Long groupId);

    @Query("select COUNT(g.user) from UserHasGroup g where g.group.id = :groupId")
    Long totalMembers(@Param("groupId")Long groupId);

    UserHasGroup findByUserIdAndGroupId(Long id,Long userGroupId);


    //sya
    @Query("SELECT uhg FROM UserHasGroup uhg WHERE uhg.group.id = :groupId AND uhg.user.id <> :groupOwnerId")
    List<UserHasGroup> findByGroupIdAndUserIdNot(@Param("groupId") Long groupId, @Param("groupOwnerId") Long groupOwnerId);

    @Query("SELECT uhg FROM UserHasGroup uhg WHERE uhg.group.id = :groupId AND uhg.user.id = :userId")
    UserHasGroup findChangeUser(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserHasGroup ugh WHERE ugh.group.id = :groupId AND ugh.user.id = :userId")
    void deleteUserFromGroup(@Param("userId")Long userId,@Param("groupId")Long groupId);
}
