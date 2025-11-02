package com.study.FlowTrack.controller;

import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.task.TaskCreationDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.payload.task.TaskUpdateDto;
import com.study.FlowTrack.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/task")
public class TaskController {
    private final TaskService taskService;

    @PostMapping()
    public ResponseEntity<TaskResponseDto> createTask(@AuthenticationPrincipal User creator,
                                                      @RequestBody TaskCreationDto dto) {
        TaskResponseDto tasks = taskService.createTask(creator, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{projectKey}/{taskNumber}")
                .buildAndExpand(tasks.getProjectKey(), tasks.getTaskNumber())
                .toUri();

        return ResponseEntity
                .created(location) // 201 Created
                .body(tasks);
    }

    @GetMapping("/{projectKey}/{taskNumber}")
    public ResponseEntity<TaskResponseDto> getTaskByProjectKeyAndNumber(@AuthenticationPrincipal User requester,
                                                                        @PathVariable String projectKey,
                                                                        @PathVariable Long taskNumber) {
        TaskResponseDto taskResponseDto = taskService.getTaskByProjectKeyAndNumber(requester, projectKey, taskNumber);
        return ResponseEntity.ok(taskResponseDto);
    }

    @PatchMapping("/{projectKey}/{taskNumber}")
    public ResponseEntity<TaskResponseDto> updateTask(@AuthenticationPrincipal User requester,
                                                      @PathVariable String projectKey,
                                                      @PathVariable Long taskNumber,
                                                      @RequestBody TaskUpdateDto dto) {
        TaskResponseDto taskResponseDto = taskService.updateTask(requester, projectKey, taskNumber, dto);
        return ResponseEntity.ok(taskResponseDto);
    }

    @DeleteMapping("/{projectKey}/{taskNumber}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal User requester,
                                           @PathVariable String projectKey,
                                           @PathVariable Long taskNumber) {
        taskService.deleteTask(requester, projectKey, taskNumber);
        return ResponseEntity.noContent().build();
    }
}
