package com.study.FlowTrack.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_name",unique = true,nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(name = "register_Date")
    private LocalDateTime registerDate;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnable = true;

    @Column(name = "is_account_non_locked", nullable = false)
    private boolean isAccountNonLocked = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.systemRoles.stream()
                .map(systemRole -> new SimpleGrantedAuthority(systemRole.getName().name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnable;
    }

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<SystemRoleEntity> systemRoles = new HashSet<>();

    @OneToMany(mappedBy = "creator")
    private Set<Task> createdTasks = new HashSet<>();

    @OneToMany(mappedBy = "assignedUser")
    private Set<Task> assignedTasks = new HashSet<>();

    @OneToMany(mappedBy = "creator")
    private Set<Project> createdProjects = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMembership> userMemberships = new HashSet<>();


}
