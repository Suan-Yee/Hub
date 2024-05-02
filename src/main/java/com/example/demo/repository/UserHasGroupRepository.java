package com.example.demo.repository;


import com.example.demo.entity.Group;
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


    List<UserHasGroup> findByGroupId(Long groupId);

    @Query (value = "select distinct group _id from user_has_group where user_id = :userId",nativeQuery = true)
    List<Long> findDistinctGroupIdByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserHasGroup ug WHERE ug.group.id = :groupId AND ug.user.id = :userId")
    void deleteByCommunityIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);


    @Query("SELECT g.group.id FROM UserHasGroup g where g.user.id = :userId ")
    List<Long> groupIdByUserId(@Param("userId")Long userId);

}
