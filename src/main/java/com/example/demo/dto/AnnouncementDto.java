package com.example.demo.dto;

import com.example.demo.entity.Announcement;
import lombok.Builder;

@Builder
public record AnnouncementDto(
        Long id,
        String title,
        String message,
        String announcementStartDate,
        String announcementEndDate,
        String photo
) {
    public AnnouncementDto(Announcement announcement) {
        this(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getMessage(),
                announcement.getAnnouncementStartDate(),
                announcement.getAnnouncementEndDate(),
                announcement.getPhoto()
        );
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getAnnouncementStartDate() {
        return announcementStartDate;
    }

    public String getAnnouncementEndDate() {
        return announcementEndDate;
    }

    public String getPhoto() {
        return photo;
    }
}
