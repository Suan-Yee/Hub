package com.example.demo.dto;


import com.example.demo.entity.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AnnouncementDto {

    private Long id;
    private String title;
    private String message;
    private String announcementStartDate;
    private String announcementEndDate;
    private String photo;

    public AnnouncementDto(Announcement announcement){
        this.id=announcement.getId();
        this.title=announcement.getTitle();
        this.message=announcement.getMessage();
        this.announcementStartDate=announcement.getAnnouncementStartDate();
        this.announcementEndDate=announcement.getAnnouncementEndDate();
        this.photo=announcement.getPhoto();
    }
}
