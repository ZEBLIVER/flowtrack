package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.exception.DuplicateResourceException;
import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.ProjectMapper;
import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.ProjectMembership;
import com.study.FlowTrack.model.Task;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.project.ProjectCreationDto;
import com.study.FlowTrack.payload.project.ProjectResponseDto;
import com.study.FlowTrack.payload.project.ProjectUpdateDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.payload.user.UserResponseDto;
import com.study.FlowTrack.repository.ProjectMembershipRepository;
import com.study.FlowTrack.repository.ProjectRepository;
import com.study.FlowTrack.repository.TaskRepository;
import com.study.FlowTrack.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

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

    public void deleteProject(User requester, Long id) {
        Project project = getProjectEntityById(id);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        requireProjectAdmin(requesterMembership);
        projectRepository.deleteById(id);
    }

    public void addUserToProject(User requester, Long userIdToAdd, Long projectId, ProjectRole projectRole) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        requireProjectAdmin(requesterMembership);
        User userToAddOptional = userRepository.findById(userIdToAdd).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        if (projectMembershipRepository.existsByUserAndProject(userToAddOptional, project)) {
            throw new DuplicateResourceException("User already in project!");
        }
        ProjectMembership newMembership = new ProjectMembership();
        newMembership.setUser(userToAddOptional);
        newMembership.setProject(project);
        newMembership.setProjectRole(projectRole);

        projectMembershipRepository.save(newMembership);
    }

    public void deleteUserFromProject(User requester, Long userIdToDelete, Long projectId) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        requireProjectAdmin(requesterMembership);
        User userToDelete = userRepository.findById(userIdToDelete).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        ProjectMembership projectMembershipToDelete = projectMembershipRepository.
                findByUserAndProject(userToDelete, project).orElseThrow(
                        () -> new ResourceNotFoundException("User is missing in project")
                );
        projectMembershipRepository.delete(projectMembershipToDelete);

    }

    public ProjectResponseDto getProjectById(User requester, Long id) {
        Project project = getProjectEntityById(id);
        requireUserMembership(requester, project);
        return projectMapper.toResponseDto(project);
    }

    public List<ProjectResponseDto> getAllProjects(User user) {
        List<ProjectMembership> memberships = projectMembershipRepository.findByUser(user);

        List<Project> projects = memberships.stream()
                .map(ProjectMembership::getProject)
                .collect(Collectors.toList());

        return projectMapper.toResponseDtoList(projects);
    }

    public void setUserRolesInProject(User requester, Long userIdToUpdate, Long projectId, ProjectRole role) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);

        requireProjectAdminOrProductManager(requesterMembership);
        User userToUpdate = userRepository.findById(userIdToUpdate).orElseThrow(
                () -> new ResourceNotFoundException("User to update not found.")
        );
        ProjectMembership userToUpdateMembership = projectMembershipRepository.
                findByUserAndProject(userToUpdate, project).orElseThrow(
                        () -> new ResourceNotFoundException("User is not a member of the project.")
                );
        if (requester.getId().equals(userIdToUpdate)) {
            throw new PermissionDeniedException("Cannot change your own role.");
        }

        if (project.getCreator().getId().equals(userIdToUpdate) &&
                !project.getCreator().getId().equals(requester.getId())) {
            throw new PermissionDeniedException("Only the project creator can change their own role.");
        }

        int requesterLevel = requesterMembership.getProjectRole().getPrivilegeLevel();
        int userToUpdateLevel = userToUpdateMembership.getProjectRole().getPrivilegeLevel();
        if (requesterLevel <= userToUpdateLevel) {
            throw new PermissionDeniedException("Cannot change the role of a user with equal or higher privilege level.");
        }
        if (requesterLevel < role.getPrivilegeLevel()) {
            throw new PermissionDeniedException("Cannot grant a role with a higher privilege level than your own.");
        }

        userToUpdateMembership.setProjectRole(role);
        projectMembershipRepository.save(userToUpdateMembership);
    }

    public void updateProject(User requester, Long projectId, ProjectUpdateDto dto) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        requireProjectAdminOrProductManager(requesterMembership);
        if (dto.getName() != null) project.setName(dto.getName());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());

        projectRepository.save(project);
    }

    public List<TaskResponseDto> getAllTasksInProject(User requester, Long projectId) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        List<Task> tasks = taskRepository.findAllByProject(project);

        return projectMapper.toTaskResponseDtoList(tasks);
    }

    public List<UserResponseDto> getAllUsersInProject(User requester, Long projectId) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);

        List<ProjectMembership> memberships = projectMembershipRepository.findByProject(project);

        return memberships.stream().map(projectMapper::toResponseUserDto).collect(Collectors.toList());
    }

    private Project getProjectEntityById(Long id) {
        return projectRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Project with id: " + id + " not found")
        );
    }

    private ProjectMembership requireUserMembership(User requester, Project project) {
        return projectMembershipRepository.findByUserAndProject(requester, project).orElseThrow(
                () -> new PermissionDeniedException("Access denied: Must be a participant in the project")
        );
    }

    private void requireProjectAdmin(ProjectMembership requesterMembership) {
        if (!requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN)) {
            throw new PermissionDeniedException("Access denied: not enough rights.");
        }
    }

    private void requireProjectAdminOrProductManager(ProjectMembership requesterMembership) {
        if (!requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN) &&
                !requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_PRODUCT_MANAGER)) {
            throw new PermissionDeniedException("Access denied: not enough rights.");
        }
    }


}
