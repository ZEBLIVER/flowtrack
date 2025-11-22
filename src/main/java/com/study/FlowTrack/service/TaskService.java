package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.StatusTask;
import com.study.FlowTrack.event.TaskStatusChangedEvent;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.TaskMapper;
import com.study.FlowTrack.model.*;
import com.study.FlowTrack.payload.task.TaskCreationDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.payload.task.TaskUpdateDto;
import com.study.FlowTrack.repository.*;
import com.study.FlowTrack.service.kafka.KafkaProducerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final StatusTaskEntityRepository statusTaskEntityRepository;
    private final ProjectAccessService projectAccessService;
    private final KafkaProducerService kafkaProducerService;

    @CacheEvict(value = "tasks", key = "#project.key")
    public TaskResponseDto createTask(User creator, TaskCreationDto dto) {
        Project project = projectAccessService.getProjectEntityById(dto.getProjectId());
        projectAccessService.requireUserMembership(creator, project);

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

    @Cacheable(value = "tasks", key = "#projectKey + '_' + #taskNumber")
    public TaskResponseDto getTaskByProjectKeyAndNumber(User requester, String projectKey, Long taskNumber) {
        Task task = projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber);
        projectAccessService.requireUserMembership(requester, task.getProject());
        return taskMapper.toResponseDto(task);
    }

    public TaskResponseDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Задача с ID " + taskId + " не найдена."));
        return taskMapper.toResponseDto(task);
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#projectKey"),
            @CacheEvict(value = "tasks", key = "#projectKey + '_' + #taskNumber")
    })
    public TaskResponseDto updateTask(User requester, String projectKey, Long taskNumber, TaskUpdateDto dto) {
        Task task = projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber);
        Project project = task.getProject();
        ProjectMembership membership = projectAccessService.requireUserMembership(requester, project);
        projectAccessService.requireProjectNotViewer(membership);

        StatusTaskEntity oldStatusEntity = task.getStatusTaskEntity();
        String oldStatusName = oldStatusEntity.getStatusTask().name();

        boolean statusChanged = false;
        StatusTaskEntity newStatusEntity = null;

        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());

        if (dto.getAssignedUserId() != null) {
            User user = projectAccessService.getUserById(dto.getAssignedUserId());
            projectAccessService.requireUserMembership(user, project);
            task.setAssignedUser(user);
        }

        if (dto.getNewStatusId() != null) {
             newStatusEntity = statusTaskEntityRepository.findById(dto.getNewStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Статус задачи не найден."));
             if (!oldStatusEntity.getId().equals(newStatusEntity.getId())) {
                 task.setStatusTaskEntity(newStatusEntity);
                 statusChanged = true;
             }

        }
        taskRepository.save(task);
        if (statusChanged) {
            String newStatusName = newStatusEntity.getStatusTask().name();

            TaskStatusChangedEvent event = TaskStatusChangedEvent.builder()
                    .taskId(task.getId())
                    .projectId(task.getProject().getId())
                    .userId(requester.getId())
                    .oldStatus(oldStatusName)
                    .newStatus(newStatusName)
                    .build();

            kafkaProducerService.sendTaskStatusChangedEvent(event);
        }
        return taskMapper.toResponseDto(task);
    }

    @Caching(evict = {
            @CacheEvict(value = "tasks", key = "#projectKey"),
            @CacheEvict(value = "tasks", key = "#projectKey + '_' + #taskNumber")
    })
    public void deleteTask(User requester, String projectKey, Long taskNumber) {
        Task task = projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber);
        Project project = task.getProject();
        ProjectMembership membership = projectAccessService.requireUserMembership(requester, project);
        projectAccessService.requireProjectAdminOrProductManager(membership);
        taskRepository.delete(task);
    }

}
