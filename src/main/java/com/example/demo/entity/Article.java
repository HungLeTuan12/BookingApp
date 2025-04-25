package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "article")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String title;

    private String content;

    @Column(length = 128)
    private String image;

    @Column(length = 64)
    private String url_id;

    @UpdateTimestamp
    private LocalDateTime update_at;

    @CreationTimestamp
    private LocalDateTime create_at;

    @ManyToOne
    private User user;
}
