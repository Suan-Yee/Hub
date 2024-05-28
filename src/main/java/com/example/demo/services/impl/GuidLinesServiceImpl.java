package com.example.demo.services.impl;

import com.example.demo.entity.GuideLines;
import com.example.demo.repository.GuideLinesResposity;
import com.example.demo.services.GuideLinesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuidLinesServiceImpl implements GuideLinesService {
    private final GuideLinesResposity guideLinesResposity;

    @Override
    public GuideLines save(GuideLines guideLines) {
        return guideLinesResposity.save(guideLines);
    }

    @Override
    public List<GuideLines> findAll() {
        return guideLinesResposity.findAll();
    }


}
