package com.study.FlowTrack.model;

import com.study.FlowTrack.enums.StatusTask;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tasks")
@ToString(exclude = {"comments", "statusTaskEntity", "creator", "assignedUser", "project"})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @CreationTimestamp
    @Column(name = "creation_Time",nullable = false)
    private LocalDateTime creationTime;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "task_number", nullable = false)
    private Long taskNumber;

    @OneToMany(mappedBy = "task",cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_task_id",nullable = false)
    private StatusTaskEntity statusTaskEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id",nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id",nullable = false)
    private Project project;

}
