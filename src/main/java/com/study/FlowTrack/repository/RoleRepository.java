package com.study.FlowTrack.repository;

import com.study.FlowTrack.enums.UserRole;
import com.study.FlowTrack.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    boolean existsByName(UserRole name);

    Optional<Role> findByName(UserRole name);
}