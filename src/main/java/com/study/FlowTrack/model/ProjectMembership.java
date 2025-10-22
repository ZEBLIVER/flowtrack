package com.study.FlowTrack.model;

import com.study.FlowTrack.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "project_memberships",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "project_id"})
        })
public class ProjectMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false,unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "project_role",nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;
}
