package com.study.FlowTrack.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id",nullable = false,unique = true)
    private Long id;

    @Column(name = "project_name",nullable = false)
    private String name;

    @Column(name = "project_key",nullable = false,unique = true)
    private String key;
    private String description;


    @OneToMany(mappedBy = "project")
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMembership> memberships = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "creator_user_id", nullable = false)
    public User creator;


}
