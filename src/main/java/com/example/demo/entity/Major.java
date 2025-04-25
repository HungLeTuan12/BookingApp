package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "major")
public class Major {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100,unique = true)
    private String name;

    @Column(length = 100)
    private String avatar;

    @Column(length = 64)
    private String idImage;

    @Column(length = 255)
    private String description;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "major")
    private List<User> users = new ArrayList<>();
}