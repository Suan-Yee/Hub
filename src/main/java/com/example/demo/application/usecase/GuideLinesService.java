package com.example.demo.application.usecase;

import com.example.demo.entity.GuideLines;

import java.util.List;
import java.util.Optional;

public interface GuideLinesService {
    GuideLines save(GuideLines guideLines);
    List<GuideLines> findAll();
    Optional<GuideLines> findById(Long id);
}
