package com.example.demo.repository;

import com.example.demo.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {
    List<ContentReport> findByReporterId(Long reporterId);
    List<ContentReport> findByStatus(String status);
    List<ContentReport> findByTargetTypeAndTargetId(String targetType, Long targetId);
}
