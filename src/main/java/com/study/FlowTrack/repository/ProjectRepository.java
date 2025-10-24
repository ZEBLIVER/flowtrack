package com.study.FlowTrack.repository;

import com.study.FlowTrack.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project,Long> {

    boolean existsByName(String name);

    boolean existsByKey(String key);
}
