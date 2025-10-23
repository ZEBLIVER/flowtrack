package com.study.FlowTrack.model;

import com.study.FlowTrack.enums.StatusTask;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "statusTask")
@ToString(exclude = "tasks")
@Table(name = "status_task")
public class StatusTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_task_id")
    private Long id;

    @Column(name = "status_task",nullable = false,unique = true)
    @Enumerated(EnumType.STRING)
    private StatusTask statusTask;

    @OneToMany(mappedBy = "statusTaskEntity")
    private Set<Task> tasks = new HashSet<>();

    public StatusTaskEntity(StatusTask statusTask) {
        this.statusTask = statusTask;
    }
}
