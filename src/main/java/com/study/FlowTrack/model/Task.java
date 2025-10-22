package com.study.FlowTrack.model;

import com.study.FlowTrack.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id",nullable = false,unique = true)
    private Long id;

    @CreationTimestamp
    @Column(name = "creation_Time",nullable = false)
    private LocalDateTime creationTime;

    private String title;
    private String description;
    private TaskStatus taskStatus;

    @ManyToOne
    @JoinColumn(name = "creator_user_id",nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private User assignedUser;

    @ManyToOne
    @JoinColumn(name = "project_id",nullable = false)
    private Project project;

}
