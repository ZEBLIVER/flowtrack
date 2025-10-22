package com.study.FlowTrack.model;

import com.study.FlowTrack.enums.SystemRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class SystemRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id",updatable = false,nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private SystemRole name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    public SystemRoleEntity(SystemRole roleName) {
        this.name = roleName;
    }
}
