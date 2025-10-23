package com.study.FlowTrack.model;

import com.study.FlowTrack.enums.StatusTask;
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
@Table(name = "status_task")
public class StatusTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_task_id", nullable = false,unique = true)
    private Long id;

    @Column(name = "status_task",nullable = false,unique = true)
    @Enumerated(EnumType.STRING)
    private StatusTask statusTask;

    @OneToMany(mappedBy = "statusTaskEntity")
    private Set<Task> tasks = new HashSet<>();
}
