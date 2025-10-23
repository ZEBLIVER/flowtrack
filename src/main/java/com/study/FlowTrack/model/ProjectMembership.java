package com.study.FlowTrack.model;

import com.study.FlowTrack.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "project_memberships",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "project_id"})
        })
@EqualsAndHashCode(of = {"user", "project"})
@ToString(exclude = {"user", "project"})
public class ProjectMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "project_role",nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;
}
