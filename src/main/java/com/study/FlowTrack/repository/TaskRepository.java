package com.study.FlowTrack.repository;

import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface TaskRepository extends JpaRepository<Task,Long> {
    List<Task> findAllByProject(Project project);

    boolean existsByTitle(String title);

    Optional<Task> findFirstByProjectOrderByTaskNumberDesc(Project project);

    Optional<Task> findByProjectAndTaskNumber(Project project, Long taskNumber);
}
