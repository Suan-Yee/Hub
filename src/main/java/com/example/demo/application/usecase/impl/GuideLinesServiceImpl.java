package com.example.demo.application.usecase.impl;

import com.example.demo.entity.GuideLines;
import com.example.demo.infrastructure.persistence.repository.GuideLinesRepository;
import com.example.demo.application.usecase.GuideLinesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuideLinesServiceImpl implements GuideLinesService {
    private final GuideLinesRepository guideLinesRepository;

    @Override
    public GuideLines save(GuideLines guideLines) {
        return guideLinesRepository.save(guideLines);
    }

    @Override
    public List<GuideLines> findAll() {
        return guideLinesRepository.findAll();
    }

    @Override
    public Optional<GuideLines> findById(Long id) {
        return guideLinesRepository.findById(id);
    }
}
