package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.enums.StatusTask;
import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.TaskMapper;
import com.study.FlowTrack.model.*;
import com.study.FlowTrack.payload.task.TaskCreationDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.payload.task.TaskUpdateDto;
import com.study.FlowTrack.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    private final StatusTaskEntityRepository statusTaskEntityRepository;
    private final UserRepository userRepository;

    public TaskResponseDto createTask(User creator, TaskCreationDto dto) {
        Project project = getProjectEntityById(dto.getProjectId());
        requireUserMembership(creator, project);

        StatusTaskEntity statusTask = statusTaskEntityRepository
                .findByStatusTask(StatusTask.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("Начальный статус OPEN не найден в базе данных."));

        Long lastTaskNumber = taskRepository.findFirstByProjectOrderByTaskNumberDesc(project)
                .map(Task::getTaskNumber)
                .orElse(0L);
        Long newTaskNumberInCurrentProject = lastTaskNumber + 1;

        Task newTask = taskMapper.toEntity(dto);

        newTask.setTaskNumber(newTaskNumberInCurrentProject);
        newTask.setCreator(creator);
        newTask.setProject(project);
        newTask.setStatusTaskEntity(statusTask);

        Task savedTask = taskRepository.save(newTask);
        return taskMapper.toResponseDto(savedTask);
    }

    public TaskResponseDto getTaskById(User requester, Long taskId) {
        Task task = getTaskById(taskId);
        Project project = task.getProject();
        requireUserMembership(requester,project);

        return taskMapper.toResponseDto(task);

    }

    public TaskResponseDto getTaskByProjectKeyAndNumber(User requester, String projectKey, Long taskNumber) {
            Project project = getProjectEntityByKey(projectKey);
            requireUserMembership(requester,project);
            Task task = taskRepository.findByProjectAndTaskNumber(project,taskNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("не найдена задача"));
            return taskMapper.toResponseDto(task);
    }

    public TaskResponseDto updateTask(User requester, Long taskId, TaskUpdateDto dto) {
        Task task = getTaskById(taskId);
        Project project = task.getProject();
        ProjectMembership membership = requireUserMembership(requester,project);
        requireProjectNotViewer(membership);

        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());

        if (dto.getAssignedUserId() != null) {
            User user = getUserById(dto.getAssignedUserId());
            requireUserMembership(user,project);
            task.setAssignedUser(user);
        }

        if (dto.getNewStatusId() != null) {
            StatusTaskEntity statusTask = statusTaskEntityRepository.findById(dto.getNewStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Статус задачи не найден."));
            task.setStatusTaskEntity(statusTask);
        }
        taskRepository.save(task);
        return taskMapper.toResponseDto(task);
    }

    public void deleteTask(User requester, Long taskId) {
        Task task = getTaskById(taskId);
        Project project = task.getProject();
        ProjectMembership membership = requireUserMembership(requester,project);
        requireProjectAdminOrProductManager(membership);
        taskRepository.delete(task);
    }

    public List<TaskResponseDto> getTasksByProject(User requester, Long projectId) {
        Project project = getProjectEntityById(projectId);
        requireUserMembership(requester,project);
        List<Task> tasks = taskRepository.findAllByProject(project);
        return taskMapper.toResponseListDto(tasks);
    }

    public List<TaskResponseDto>  getTasksByAssignee(User requester, Long assignerId) {
        if (requester.getId() != assignerId) {
            throw new PermissionDeniedException("Access denied: not enough rights.");
        }
        User assignerUser = getUserById(assignerId);
        List<Task> assignedTasks = taskRepository.findAllByAssignedUser(assignerUser);

        List<Task> accessibleTasks = assignedTasks.stream()
                .filter(task -> {
                    Project project = task.getProject();
                    return projectMembershipRepository.findByUserAndProject(requester,project).isPresent();
                })
                .collect(Collectors.toList());

        return taskMapper.toResponseListDto(accessibleTasks);
    }

    public List<TaskResponseDto> getTasksByAssigneeAndProject(User requester, Long assignerId, Long projectId) {
        Project project = getProjectEntityById(projectId);
        User assignedUser = getUserById(assignerId);
        requireUserMembership(requester,project);

        List<Task> assignedTasks = taskRepository.findAllByAssignedUserAndProject(assignedUser, project);

        return taskMapper.toResponseListDto(assignedTasks);
    }

    public List<TaskResponseDto> getTasksByCreator(User requester, Long creatorId) {
        if (requester.getId() != creatorId) {
            throw new PermissionDeniedException("not enough rights");
        }
        User creator = getUserById(creatorId);
        List<Task> createdTasks = taskRepository.getAllByCreator(creator);

        List<Task> accessibleCreatedTasks = createdTasks.stream().filter(
                task -> {
                    Project project = task.getProject();
                    return projectMembershipRepository.findByUserAndProject(requester,project).isPresent();
                }
        ).collect(Collectors.toList());

        return taskMapper.toResponseListDto(accessibleCreatedTasks);
    }

    private ProjectMembership requireUserMembership(User requester, Project project) {
        return projectMembershipRepository.findByUserAndProject(requester, project).orElseThrow(
                () -> new PermissionDeniedException("Access denied: Must be a participant in the project")
        );
    }
    private Project getProjectEntityById(Long id) {
        return projectRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Project with id: " + id + " not found")
        );
    }
    private Project getProjectEntityByKey(String key) {
        return projectRepository.findProjectByKey(key).orElseThrow(
                () -> new ResourceNotFoundException("Project with key: " + key + " not found")
        );
    }
    private Task getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Task with id: " + id + " not found")
        );
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Назначенный пользователь не найден."));
    }
    private void requireProjectNotViewer(ProjectMembership requesterMembership) {
        if (requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_VIEWER)){
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
