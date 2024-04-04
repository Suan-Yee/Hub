package com.example.demo.repository;

import com.example.demo.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OTP,Long> {

    Optional<OTP> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM OTP o WHERE o.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
