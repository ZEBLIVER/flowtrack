package com.study.FlowTrack.model;

import com.study.FlowTrack.enums.SystemRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "systemRoleName")
@ToString(exclude = "users")
public class SystemRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private SystemRole systemRoleName;

    @ManyToMany(mappedBy = "systemRoles")
    private Set<User> users = new HashSet<>();

    public SystemRoleEntity(SystemRole roleName) {
        this.systemRoleName = roleName;
    }
}
