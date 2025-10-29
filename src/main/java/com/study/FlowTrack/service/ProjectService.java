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

    public void deleteProject(User user, Long id) {
        Project project = getProjectEntityById(id);
        Optional<ProjectMembership> optionalMembership = projectMembershipRepository.findByUserAndProject(user, project);
        if (optionalMembership.isEmpty()) throw new PermissionDeniedException("Access denied");
        ProjectMembership projectMembership = optionalMembership.get();
        if (!projectMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN)) {
            throw new PermissionDeniedException("Access denied: Must be Project Admin.");
        }
        projectRepository.deleteById(id);
    }

    public void addUserToProject(User user, Long userIdToAdd, Long projectId, ProjectRole projectRole) {
        Project project = getProjectEntityById(projectId);
        Optional<ProjectMembership> optionalMembership = projectMembershipRepository.findByUserAndProject(user, project);
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
        if (projectMembershipRepository.existsByUserAndProject(userToAddOptional, project)) {
            throw new DuplicateResourceException("User already in project!");
        }
        ProjectMembership newMembership = new ProjectMembership();
        newMembership.setUser(userToAddOptional);
        newMembership.setProject(project);
        newMembership.setProjectRole(projectRole);

        projectMembershipRepository.save(newMembership);
    }

    public void deleteUserFromProject(User user, Long userIdToDelete, Long projectId) {
        Project project = getProjectEntityById(projectId);
        Optional<ProjectMembership> optionalMembership = projectMembershipRepository.findByUserAndProject(user, project);
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
                findByUserAndProject(userToDelete, project).orElseThrow(
                        () -> new ResourceNotFoundException("User is missing in project")
                );
        projectMembershipRepository.delete(projectMembershipToDelete);

    }

    public ProjectResponseDto getProjectById(User user, Long id) {
        Project project = getProjectEntityById(id);
        Optional<ProjectMembership> membership = projectMembershipRepository.findByUserAndProject(user, project);
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

    public void setUserRolesInProject(User user, Long userIdToUpdate, Long projectId, ProjectRole role) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership membership = projectMembershipRepository.findByUserAndProject(user, project).orElseThrow(
                () -> new PermissionDeniedException("запрос от пользователя которого нет вообще в этом проекте")
        );
        if (!membership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN) &&
                !membership.getProjectRole().equals(ProjectRole.PROJECT_PRODUCT_MANAGER)) {
            throw new PermissionDeniedException("нет прав для данной операции");
        }
        User userToUpdate = userRepository.findById(userIdToUpdate).orElseThrow(
                () -> new ResourceNotFoundException("обновляемый пользователь не найден в бд")
        );
        ProjectMembership userToUpdateMembership = projectMembershipRepository.
                findByUserAndProject(userToUpdate, project).orElseThrow(
                        () -> new ResourceNotFoundException("связь обновляемого пользователя и проекта не найдена")
                );
        if (user.getId().equals(userIdToUpdate)) {
            throw new PermissionDeniedException("нельзя менять собственную роль");
        }

        if (project.getCreator().getId().equals(userIdToUpdate) &&
                !project.getCreator().getId().equals(user.getId())) {
            throw new PermissionDeniedException("Только Создатель проекта может менять свою роль.");
        }

        int requesterLevel = membership.getProjectRole().getPrivilegeLevel();
        int userToUpdateLevel = userToUpdateMembership.getProjectRole().getPrivilegeLevel();
        if (requesterLevel <= userToUpdateLevel) {
            throw new PermissionDeniedException("нельзя менять роль пользователя уровнем выше вашего");
        }
        if (requesterLevel < role.getPrivilegeLevel()) {
            throw  new PermissionDeniedException("нельзя выдать роль выше вашей");
        }


        userToUpdateMembership.setProjectRole(role);
        projectMembershipRepository.save(userToUpdateMembership);
    }

    public void updateProject(User requester, Long projectId, ProjectUpdateDto dto) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = projectMembershipRepository.
                findByUserAndProject(requester,project).orElseThrow(
                        () ->  new ResourceNotFoundException("не найдена связь")
                );
        if (!requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN) &&
        !requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_PRODUCT_MANAGER)) {
            throw new PermissionDeniedException("отказано в доступе");
        }
        if (dto.getName() != null) project.setName(dto.getName());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());

        projectRepository.save(project);
    }

    public List<TaskResponseDto> getAllTasksInProject(User requester, Long projectId) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = projectMembershipRepository.
                findByUserAndProject(requester,project).orElseThrow(
                        () ->  new ResourceNotFoundException("не найдена связь")
                );
        List<Task> tasks = taskRepository.findAllByProject(project);

        return projectMapper.toTaskResponseDtoList(tasks);
    }

    public List<UserResponseDto>  getAllUsersInProject(User requester, Long projectId) {
        Project project = getProjectEntityById(projectId);
        ProjectMembership requesterMembership = projectMembershipRepository.
                findByUserAndProject(requester,project).orElseThrow(
                        () ->  new ResourceNotFoundException("не найдена связь")
                );

        List<ProjectMembership> memberships = projectMembershipRepository.findByProject(project);

        return memberships.stream().map(projectMapper::toResponseUserDto).collect(Collectors.toList());
    }

    private Project getProjectEntityById(Long id) {
        return projectRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Project with id: " + id + " not found")
        );
    }


}
