package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.User;
import com.example.demo.enumeration.Access;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

        Optional<User> findByStaffId(String staffId);
        Optional<User> findEmailByStaffId(String staffId);
        Optional<User> findByEmail(String email);
        Page<User> findAll(Specification<User> spec, Pageable pageable);
        List<User> findAllByAccess(Access access);
        List<User> findByStatus(boolean b);

        List<User> findByNameContainingAndStaffIdNot(String name,String staffId);

        @Query("SELECT u FROM User u WHERE u.name = :userName")
        User findByName(@Param("userName") String name);

        @Query("SELECT u FROM User u LEFT JOIN UserHasGroup uhg ON u.id = uhg.user.id AND uhg.group.id = :groupId WHERE uhg.user.id IS NULL")
        List<User> findUsersNotInGroup(@Param("groupId") Long groupId);

        @Query("select uhg.user FROM UserHasGroup uhg WHERE uhg.group.id = :groupId")
        List<User> getAllUserFromGroup(@Param("groupId") Long groupId);

        @Query("SELECT u FROM User u JOIN UserHasGroup uhg ON u.id = uhg.user.id AND uhg.group.id = :groupId WHERE u.id = :userId")
        Optional<User> findUserInGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
