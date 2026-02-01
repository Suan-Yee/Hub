package com.example.demo.application.usecase.impl;

import com.example.demo.dto.AnnouncementDto;
import com.example.demo.entity.Announcement;
import com.example.demo.infrastructure.persistence.repository.AnnouncementRepository;
import com.example.demo.application.usecase.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);


    @Override
    public Announcement createAnnouncement(AnnouncementDto announcementdto){
        Announcement announcement=new Announcement();
        announcement.setTitle(announcementdto.getTitle());
        announcement.setAnnouncementStartDate(announcementdto.getAnnouncementStartDate());
        announcement.setAnnouncementEndDate(announcementdto.getAnnouncementEndDate());
        announcement.setMessage(announcementdto.getMessage());
        return announcementRepository.save(announcement);
    }

    @Override
    public List<AnnouncementDto> findAllAnnouncement() {
        List<Announcement> list=announcementRepository.findAll();
        List<AnnouncementDto> announcementDtoList=list.stream().map(AnnouncementDto::new).collect(Collectors.toList());
        return announcementDtoList;
    }

    @Override
    public Announcement findbyId(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }

    @Override
    public Announcement updateAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    @Override
    public void deleteAnnoucement(Long id) {
        announcementRepository.deleteById(id);
    }

    /* @Scheduled(cron = "0 0 0 * * ?")*/ // Run every day at midnight
//    @Scheduled(cron = "0 0/1 * * * ?") // Run every minute for testing
    public void deleteExpiredAnnouncements() {
        LocalDate today = LocalDate.now();
        List<Announcement> announcements = announcementRepository.findAll();

        boolean anyDeleted = false;
        for (Announcement announcement : announcements) {
            LocalDate endDate = LocalDate.parse(announcement.getAnnouncementEndDate(), formatter);
            if (endDate.isBefore(today)) {
                announcementRepository.delete(announcement);
                log.info("Deleted announcement with ID: " + announcement.getId());
                anyDeleted = true;
            }
        }

        if (!anyDeleted) {
            log.info("No expired announcements to delete.");
        }
    }
}