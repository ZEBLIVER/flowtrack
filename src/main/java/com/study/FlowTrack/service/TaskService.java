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

import java.util.Arrays;

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
            User user = userRepository.findById(dto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Назначаемый пользователь не найден."));
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
    public void assignUserToTask() {

    }
    public void removeUserFromTask() {

    }
    public void updateTaskStatus() {

    }
    public void updateTaskPriority() {

    }
    public void getTasksByProject() {

    }
    public void getTasksByAssignee() {

    }
    public void getTasksByCreator() {

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
