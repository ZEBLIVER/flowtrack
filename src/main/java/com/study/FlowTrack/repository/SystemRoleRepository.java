package com.study.FlowTrack.repository;

import com.study.FlowTrack.enums.SystemRole;
import com.study.FlowTrack.model.SystemRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemRoleRepository extends JpaRepository<SystemRoleEntity,Long> {
    boolean existsByName(SystemRole name);

    Optional<SystemRoleEntity> findByName(SystemRole name);
}