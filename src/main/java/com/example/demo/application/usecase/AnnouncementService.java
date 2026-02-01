package com.example.demo.application.usecase;

import com.example.demo.dto.AnnouncementDto;
import com.example.demo.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    Announcement createAnnouncement(AnnouncementDto announcementDto);
    List<AnnouncementDto> findAllAnnouncement();
    public Announcement findbyId(Long id);
    Announcement updateAnnouncement(Announcement announcementdto);
    void deleteAnnoucement(Long id);
}
