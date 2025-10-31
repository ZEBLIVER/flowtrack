package com.study.FlowTrack.repository;

import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.ProjectMembership;
import com.study.FlowTrack.model.Task;
import com.study.FlowTrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project,Long> {

    boolean existsByName(String name);

    boolean existsByKey(String key);

    Optional<Project> findProjectByKey(String key);
}
