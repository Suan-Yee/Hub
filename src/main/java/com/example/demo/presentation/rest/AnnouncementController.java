package com.example.demo.presentation.rest;

import com.example.demo.dto.AnnouncementDto;
import com.example.demo.entity.Announcement;
import com.example.demo.application.usecase.AnnouncementService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor @Slf4j
@RequestMapping("/announcement")
public class AnnouncementController {

    private final AnnouncementService announcementService;


    @PostMapping("/createAnnouncement")
    public ResponseEntity<?> createAnnouncement(@RequestBody AnnouncementDto announcement) {
        log.info("CreateAnnouncement reached mee mee{}",announcement);
        Announcement createdAnnouncement = announcementService.createAnnouncement(announcement);
        if (createdAnnouncement != null) {
            return ResponseEntity.ok(createdAnnouncement);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/showAnnouncement")
    public ResponseEntity<?> getAllAnnouncement(){
        List<AnnouncementDto> announcements = announcementService.findAllAnnouncement();
        log.info(String.valueOf(announcements));
        return ResponseEntity.ok(announcements);
    }


    @GetMapping("/setUpeditevent/{id}")
    public ResponseEntity<?> UpdateForm(@PathVariable Long id){
        Announcement announcement=announcementService.findbyId(id);
        if (announcement != null) {
            return ResponseEntity.ok(announcement);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editAnnouncement(@PathVariable Long id,@RequestBody AnnouncementDto announcementDto) {
        if(announcementDto !=null){
            Announcement announcement = announcementService.findbyId(id);
            announcement.setTitle(announcementDto.getTitle());
            announcement.setAnnouncementEndDate(announcementDto.getAnnouncementEndDate());
            announcement.setMessage(announcementDto.getMessage());
            announcement.setAnnouncementStartDate(announcementDto.getAnnouncementStartDate());
            Announcement editannouncement = announcementService.updateAnnouncement(announcement);
            if (editannouncement != null) {
                return ResponseEntity.ok(HttpStatus.OK);
            }
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping ("/delete/{id}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable Long id) {
        log.info("Delete Announcement");
        announcementService.deleteAnnoucement(id);
        return ResponseEntity.ok("Announcement with id " + id + " deleted successfully");
    }
}



