package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.exception.DuplicateResourceException;
import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.ProjectMapper;
import com.study.FlowTrack.mapper.TaskMapper;
import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.ProjectMembership;
import com.study.FlowTrack.model.Task;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.project.ProjectCreationDto;
import com.study.FlowTrack.payload.project.ProjectMembershipRequestDto;
import com.study.FlowTrack.payload.project.ProjectResponseDto;
import com.study.FlowTrack.payload.project.ProjectUpdateDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.payload.user.UserDeletionRequestDto;
import com.study.FlowTrack.payload.user.UserResponseDto;
import com.study.FlowTrack.payload.user.UserRoleUpdateRequestDto;
import com.study.FlowTrack.repository.ProjectMembershipRepository;
import com.study.FlowTrack.repository.ProjectRepository;
import com.study.FlowTrack.repository.TaskRepository;
import com.study.FlowTrack.repository.UserRepository;
import com.study.FlowTrack.repository.specifications.TaskSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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
    private final TaskMapper taskMapper;
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

    public void deleteProject(User requester, String projectKey) {
        Project project = getProjectEntityByKey(projectKey);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        requireProjectAdmin(requesterMembership);
        projectRepository.deleteById(project.getId());
    }

    public void addUserToProject(User requester, String projectKey, ProjectMembershipRequestDto dto) {
        Project project = getProjectEntityByKey(projectKey);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        requireProjectAdmin(requesterMembership);
        User userToAddOptional = userRepository.findById(dto.getUserIdToAdd()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        if (projectMembershipRepository.existsByUserAndProject(userToAddOptional, project)) {
            throw new DuplicateResourceException("User already in project!");
        }
        ProjectMembership newMembership = new ProjectMembership();
        newMembership.setUser(userToAddOptional);
        newMembership.setProject(project);
        newMembership.setProjectRole(dto.getProjectRole());

        projectMembershipRepository.save(newMembership);
    }

    public void deleteUserFromProject(User requester, String projectKey, UserDeletionRequestDto dto) {
        Project project = getProjectEntityByKey(projectKey);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        requireProjectAdmin(requesterMembership);
        User userToDelete = userRepository.findById(dto.getUserIdToDelete()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        ProjectMembership projectMembershipToDelete = projectMembershipRepository.
                findByUserAndProject(userToDelete, project).orElseThrow(
                        () -> new ResourceNotFoundException("User is missing in project")
                );
        projectMembershipRepository.delete(projectMembershipToDelete);

    }

    public ProjectResponseDto getProjectByKey(User requester, String projectKey) {
        Project project = getProjectEntityByKey(projectKey);
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

    public void setUserRolesInProject(User requester, String projectKey, UserRoleUpdateRequestDto dto) {
        Project project = getProjectEntityByKey(projectKey);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);

        requireProjectAdminOrProductManager(requesterMembership);
        User userToUpdate = userRepository.findById(dto.getUserIdToUpdate()).orElseThrow(
                () -> new ResourceNotFoundException("User to update not found.")
        );
        ProjectMembership userToUpdateMembership = projectMembershipRepository.
                findByUserAndProject(userToUpdate, project).orElseThrow(
                        () -> new ResourceNotFoundException("User is not a member of the project.")
                );
        if (requester.getId().equals(dto.getUserIdToUpdate())) {
            throw new PermissionDeniedException("Cannot change your own role.");
        }

        if (project.getCreator().getId().equals(dto.getUserIdToUpdate()) &&
                !project.getCreator().getId().equals(requester.getId())) {
            throw new PermissionDeniedException("Only the project creator can change their own role.");
        }

        int requesterLevel = requesterMembership.getProjectRole().getPrivilegeLevel();
        int userToUpdateLevel = userToUpdateMembership.getProjectRole().getPrivilegeLevel();
        if (requesterLevel <= userToUpdateLevel) {
            throw new PermissionDeniedException("Cannot change the role of a user with equal or higher privilege level.");
        }
        if (requesterLevel < dto.getRole().getPrivilegeLevel()) {
            throw new PermissionDeniedException("Cannot grant a role with a higher privilege level than your own.");
        }

        userToUpdateMembership.setProjectRole(dto.getRole());
        projectMembershipRepository.save(userToUpdateMembership);
    }

    public void updateProject(User requester, String projectKey, ProjectUpdateDto dto) {
        Project project = getProjectEntityByKey(projectKey);
        ProjectMembership requesterMembership = requireUserMembership(requester, project);
        requireProjectAdminOrProductManager(requesterMembership);
        if (dto.getName() != null) project.setName(dto.getName());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());

        projectRepository.save(project);
    }

    public List<TaskResponseDto> getAllTasksInProject(User requester, String projectKey) {
        Project project = getProjectEntityByKey(projectKey);
        requireUserMembership(requester, project);
        List<Task> tasks = taskRepository.findAllByProject(project);

        return projectMapper.toTaskResponseDtoList(tasks);
    }

    public List<UserResponseDto> getAllUsersInProject(User requester, String projectKey) {
        Project project = getProjectEntityByKey(projectKey);
        requireUserMembership(requester, project);

        List<ProjectMembership> memberships = projectMembershipRepository.findByProject(project);

        return memberships.stream().map(projectMapper::toResponseUserDto).collect(Collectors.toList());
    }

    public List<TaskResponseDto>  getFilteredTasksInProject(User requester, String projectKey,
                                                            Long creatorId, Long assignerId) {
        Project project = getProjectEntityByKey(projectKey);
        requireUserMembership(requester,project);

        User assignedUser = (assignerId != null) ? getUserById(assignerId) : null;
        User creatorUser = (creatorId != null) ? getUserById(creatorId) : null;

        Specification<Task> spec = TaskSpecifications.hasProject(project);
        if (assignedUser != null) {
            spec = spec.and(TaskSpecifications.hasAssignedUser(assignedUser));
        }
        if (creatorUser != null) {
            spec = spec.and(TaskSpecifications.hasCreator(creatorUser));
        }

        List<Task> filteredTasks = taskRepository.findAll(spec);

        return taskMapper.toResponseListDto(filteredTasks);
    }

    private Project getProjectEntityByKey(String key) {
        return projectRepository.findProjectByKey(key).orElseThrow(
                () -> new ResourceNotFoundException("Project with key: " + key + " not found"));
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

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден."));
    }




}
