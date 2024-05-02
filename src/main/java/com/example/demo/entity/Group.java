package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.multipart.MultipartFile;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "`group`")
@EntityListeners(AuditingEntityListener.class)
public class Group implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "rule", nullable = false)
    private String rule;

    private String image;

    @Transient
    private MultipartFile file;

    private boolean isActive;
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    private User groupOwner;

    @Transient
    private ArrayList<Long> user;

    @OneToMany(mappedBy = "group")
    @JsonIgnore
    private List<GroupMessage> groupMessages;

    @OneToMany(mappedBy = "group")
    @JsonIgnore
    private List<Post> posts;


    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserHasGroup> userHasGroups;


    @Column(name = "deleted")
    private boolean deleted;
}
