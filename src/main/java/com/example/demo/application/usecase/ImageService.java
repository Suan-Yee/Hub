package com.example.demo.application.usecase;

import com.example.demo.entity.Content;
import com.example.demo.entity.Image;

import java.util.List;

public interface ImageService {

    Image createImage(String imageUrl, Content content);

    List<Image> findByContentId(Long id);

    void deleteImage(Long imageId);

    void deleteImageByUrl(String imageUrl,Long contentId);
}
