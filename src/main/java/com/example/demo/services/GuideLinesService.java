package com.example.demo.services;

import com.example.demo.entity.GuideLines;

import java.util.List;

public interface GuideLinesService {
    GuideLines save(GuideLines guideLines);
    List<GuideLines> findAll();
}
