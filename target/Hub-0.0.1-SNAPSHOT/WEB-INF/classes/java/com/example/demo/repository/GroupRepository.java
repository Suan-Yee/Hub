package com.example.demo.repository;


import com.example.demo.entity.Group;
import com.example.demo.entity.User;
import com.example.demo.entity.UserHasGroup;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group,Long>, JpaSpecificationExecutor<Group> {

    @Query("select g FROM  Group g where g.deleted = FALSE ")
    List<Group> findAllGroup();

    @Query("SELECT g FROM Group g WHERE g.deleted = FALSE AND g.id IN (SELECT ug.group.id FROM UserHasGroup ug WHERE ug.user.id = :userId)")
    List<Group> findGroupsByUserId(@Param("userId") Long userId);

    @Query(value = "select g.* from `group` g left join post p on g.id=p.group_id where g.deleted = false group by g.id order by count(p.id) desc limit 5",nativeQuery = true)
    List<Group> findTop5ByPostCount();

    @Query(value = "SELECT * FROM communityhub.group g WHERE g.deleted = false AND g.id NOT IN (SELECT group_id FROM user_has_group WHERE user_id = :userId) ORDER BY RAND() LIMIT 2", nativeQuery = true)
    List<Group> getRandomGroupsNotInUser(@Param("userId") Long userId);

    @Query(value="Select g.* from `group` g left join user_has_group u on g.id=u.group_id where g.deleted = false group by  g.id order by  count(u.group_id) desc limit 5",nativeQuery=true)
    List<Group> findTop5ByMemberCount();

    @Query("SELECT MONTH (p.createdAt) AS month, COUNT(p) AS postCount " +
            "FROM Post p WHERE p.group.id = :groupId AND YEAR(p.createdAt) = :year " +
            "GROUP BY MONTH(p.createdAt) " +
            "ORDER BY MONTH(p.createdAt)")
    List<Object[]> findPostCountByGroupAndYear(Long groupId, int year);

    List<Group> findAllByDeletedIsTrue();
}