package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.exception.DuplicateResourceException;
import com.study.FlowTrack.mapper.ProjectMapper;
import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.ProjectMembership;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.project.ProjectCreationDto;
import com.study.FlowTrack.payload.project.ProjectResponseDto;
import com.study.FlowTrack.repository.ProjectMembershipRepository;
import com.study.FlowTrack.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectMapper projectMapper;

    public ProjectResponseDto createProject(ProjectCreationDto creationDto, User creator) {
        if (projectRepository.existsByKey(creationDto.getKey())) {
            throw new DuplicateResourceException("Project with key '" + creationDto.getKey() + "' already exists.");
        }

        Project newProject = projectMapper.toEntity(creationDto);
        newProject.setCreator(creator);

        Project savedProject = projectRepository.save(newProject);

        ProjectMembership membership = new ProjectMembership();
        membership.setProject(savedProject);
        membership.setUser(creator);
        membership.setProjectRole(ProjectRole.PROJECT_ADMIN);

        projectMembershipRepository.save(membership);

        return projectMapper.toResponseDto(savedProject);
    }

    public void deleteProject() {

    }

    public void addUserToProject() {

    }

    public void deleteUserFromProject() {

    }

    public void getProjectById() {

    }

    public List<ProjectResponseDto> getAllProjects(User user) {
        List<ProjectMembership> memberships = projectMembershipRepository.findByUser(user);

        List<Project> projects = memberships.stream()
                .map(ProjectMembership::getProject)
                .collect(Collectors.toList());

        return projectMapper.toResponseDtoList(projects);
    }

    public void setUserRolesInProject() {

    }

    public void updateProject() {

    }

    public void getAllTasksInProject() {

    }

    public void getAllUsersInProject() {

    }


}
