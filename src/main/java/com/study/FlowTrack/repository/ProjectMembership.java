package com.study.FlowTrack.repository;

import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMembership extends JpaRepository<ProjectMembership,Long> {
    //Проверка прав
    Optional<ProjectMembership> findByUserAndProject(User user, Project project);

    //Получение списка всех участников
    List<ProjectMembership> findByProject(Project project);

    //Получение всех проектов пользователя
    List<ProjectMembership> findByUser(User user);
}
