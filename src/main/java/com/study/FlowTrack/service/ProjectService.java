package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.exception.DuplicateResourceException;
import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.ProjectMapper;
import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.ProjectMembership;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.project.ProjectCreationDto;
import com.study.FlowTrack.payload.project.ProjectResponseDto;
import com.study.FlowTrack.repository.ProjectMembershipRepository;
import com.study.FlowTrack.repository.ProjectRepository;
import com.study.FlowTrack.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;

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

    public void deleteProject(User user, Long id) {
        Project project = getProjectEntityById(id);
        Optional<ProjectMembership> optionalMembership = projectMembershipRepository.findByUserAndProject(user,project);
        if (optionalMembership.isEmpty()) throw new PermissionDeniedException("Access denied");
        ProjectMembership projectMembership = optionalMembership.get();
        if (!projectMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN)) {
            throw new PermissionDeniedException("Access denied: Must be Project Admin.");
        }
        projectRepository.deleteById(id);
    }

    public void addUserToProject(User user,Long userIdToAdd, Long projectId, ProjectRole projectRole) {
        Project project = getProjectEntityById(projectId);
        Optional<ProjectMembership> optionalMembership = projectMembershipRepository.findByUserAndProject(user,project);
        if (optionalMembership.isEmpty()) {
            throw new PermissionDeniedException("Access denied: Must be a participant in the project ");
        }
        ProjectMembership projectMembership = optionalMembership.get();
        if (!projectMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN)) {
            throw new PermissionDeniedException("Access denied: Must be Project Admin.");
        }
        User userToAddOptional = userRepository.findById(userIdToAdd).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        if (projectMembershipRepository.existsByUserAndProject(userToAddOptional,project)) {
            throw new DuplicateResourceException("User already in project!");
        }
        ProjectMembership newMembership = new ProjectMembership();
        newMembership.setUser(userToAddOptional);
        newMembership.setProject(project);
        newMembership.setProjectRole(projectRole);

        projectMembershipRepository.save(newMembership);
    }

    public void deleteUserFromProject(User user,Long userIdToDelete, Long projectId) {
        Project project = getProjectEntityById(projectId);
        Optional<ProjectMembership> optionalMembership = projectMembershipRepository.findByUserAndProject(user,project);
        if (optionalMembership.isEmpty()) {
            throw new PermissionDeniedException("Access denied: Must be a participant in the project ");
        }
        ProjectMembership projectMembership = optionalMembership.get();
        if (!projectMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN)) {
            throw new PermissionDeniedException("Access denied: Must be Project Admin.");
        }
        User userToDelete = userRepository.findById(userIdToDelete).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        ProjectMembership projectMembershipToDelete = projectMembershipRepository.
                findByUserAndProject(userToDelete,project).orElseThrow(
                        () -> new ResourceNotFoundException("User is missing in project")
                );
        projectMembershipRepository.delete(projectMembershipToDelete);

    }

    public ProjectResponseDto getProjectById(User user, Long id) {
        Project project = getProjectEntityById(id);
        Optional<ProjectMembership> membership = projectMembershipRepository.findByUserAndProject(user,project);
        if (membership.isEmpty()) throw new PermissionDeniedException("Access denied");
        return projectMapper.toResponseDto(project);
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

    private Project getProjectEntityById(Long id){
        return projectRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Project with id: "+id+" not found")
        );
    }


}
