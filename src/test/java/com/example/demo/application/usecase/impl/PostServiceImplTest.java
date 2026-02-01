package com.example.demo.application.usecase.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

//    @Mock
//    private PostRepository postRepository;
//
//    @InjectMocks
//    private PostServiceImpl postService;

//    @Test
//    void findByUserName() {
//        User user = User.builder().id(1L).name("lucas").staffId("99-00012").email("mgsuansyeey@gmail.com").role(Role.ADMIN).build();
//
//        Image image = Image.builder().id(1).name("hello.jpg").build();
//        List<Image> images = Arrays.asList(image);
//        Content content = Content.builder().id(1L).text("mocktext")
//                .images(images).build();
//        Post post = Post.builder()
//                .id(1L).status(true).content(content)
//                .user(user).build();
//        List<Post> posts = Arrays.asList(post);
//
//        Mockito.when(postRepository.findByUserName("lucas")).thenReturn(posts);
//
//        List<Post> result = postService.findByUserName("lucas");
//        Mockito.verify(postRepository).findByUserName("lucas");
//
//        assertNotNull(result);
//        assertThat(result).contains(post);
//
//    }

    @Test
    void findByTopic() {
    }
}