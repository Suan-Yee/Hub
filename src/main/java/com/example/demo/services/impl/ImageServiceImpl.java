package com.example.demo.services.impl;

import com.example.demo.entity.Content;
import com.example.demo.entity.Image;
import com.example.demo.repository.ImageRepository;
import com.example.demo.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private  final ImageRepository imageRepository;

    @Override
    public Image createImage(String imageUrl, Content content) {
        Image newImage=new Image();
        newImage.setName(imageUrl);
        newImage.setContent(content);
        return imageRepository.save(newImage);
    }

    @Override
    public List<Image> findByContentId(Long id) {
        return imageRepository.findByContent_Id(id);
    }
}
