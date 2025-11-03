package com.study.FlowTrack.controller;

import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.task.TaskCreationDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.payload.task.TaskUpdateDto;
import com.study.FlowTrack.service.TaskService;
import com.study.FlowTrack.util.TaskIdentifier;
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
    public ResponseEntity<TaskResponseDto> createTask(
            @AuthenticationPrincipal User creator,
            @RequestBody TaskCreationDto dto) {
        TaskResponseDto task = taskService.createTask(creator, dto);

        TaskIdentifier taskIdentifier = new TaskIdentifier(task.getProjectKey(), task.getTaskNumber());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{taskIdentifier}")
                .buildAndExpand(taskIdentifier.getFullKey())
                .toUri();

        return ResponseEntity
                .created(location) // 201 Created
                .body(task);
    }

    @GetMapping("/{taskIdentifier}")
    public ResponseEntity<TaskResponseDto> getTaskByProjectKeyAndNumber(
            @AuthenticationPrincipal User requester,
            @PathVariable TaskIdentifier taskIdentifier) {
        TaskResponseDto taskResponseDto = taskService.getTaskByProjectKeyAndNumber(
                requester, taskIdentifier.getProjectKey(), taskIdentifier.getTaskNumber());
        return ResponseEntity.ok(taskResponseDto);
    }

    @PatchMapping("/{taskIdentifier}")
    public ResponseEntity<TaskResponseDto> updateTask(@AuthenticationPrincipal User requester,
                                                      @PathVariable TaskIdentifier taskIdentifier,
                                                      @RequestBody TaskUpdateDto dto) {
        TaskResponseDto taskResponseDto = taskService.updateTask(
                requester, taskIdentifier.getProjectKey(), taskIdentifier.getTaskNumber(), dto);
        return ResponseEntity.ok(taskResponseDto);
    }

    @DeleteMapping("/{taskIdentifier}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal User requester,
                                           @PathVariable TaskIdentifier taskIdentifier) {
        taskService.deleteTask(requester, taskIdentifier.getProjectKey(), taskIdentifier.getTaskNumber());
        return ResponseEntity.noContent().build();
    }
}
