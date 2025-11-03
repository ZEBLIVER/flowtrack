package com.study.FlowTrack.controller;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.project.ProjectCreationDto;
import com.study.FlowTrack.payload.project.ProjectMembershipRequestDto;
import com.study.FlowTrack.payload.project.ProjectResponseDto;
import com.study.FlowTrack.payload.project.ProjectUpdateDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.payload.user.UserDeletionRequestDto;
import com.study.FlowTrack.payload.user.UserResponseDto;
import com.study.FlowTrack.payload.user.UserRoleUpdateRequestDto;
import com.study.FlowTrack.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping()
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody @Validated ProjectCreationDto creationDto,
            @AuthenticationPrincipal User creator) {
        ProjectResponseDto dto = projectService.createProject(creationDto, creator);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{key}")
                .buildAndExpand(dto.getKey())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(dto);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteProject(@AuthenticationPrincipal User requester,
                                              @PathVariable("key") String projectKey) {
        projectService.deleteProject(requester, projectKey);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectKey}/members")
    public ResponseEntity<Void> addUserToProject(@AuthenticationPrincipal User requester,
                                                 @PathVariable String projectKey,
                                                 @RequestBody @Validated ProjectMembershipRequestDto dto) {
        projectService.addUserToProject(requester, projectKey, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectKey}/members")
    public ResponseEntity<Void> deleteUserFromProject(@AuthenticationPrincipal User requester,
                                                      @PathVariable String projectKey,
                                                      @RequestBody @Validated UserDeletionRequestDto dto) {
        projectService.deleteUserFromProject(requester, projectKey, dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectKey}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@AuthenticationPrincipal User requester,
                                                             @PathVariable String projectKey) {
        ProjectResponseDto dto = projectService.getProjectByKey(requester, projectKey);
        return ResponseEntity.ok(dto);
    }

    @GetMapping()
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects(@AuthenticationPrincipal User user) {
        List<ProjectResponseDto> listOfDto = projectService.getAllProjects(user);
        return ResponseEntity.ok(listOfDto);
    }

    @PutMapping("/{projectKey}/roles")
    public ResponseEntity<Void> setUserRolesInProject(@AuthenticationPrincipal User requester,
                                                      @PathVariable String projectKey,
                                                      @RequestBody @Validated UserRoleUpdateRequestDto dto) {
        projectService.setUserRolesInProject(requester, projectKey, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{projectKey}")
    public ResponseEntity<Void> updateProject(@AuthenticationPrincipal User requester,
                                              @PathVariable String projectKey,
                                              @RequestBody @Validated ProjectUpdateDto dto) {
        projectService.updateProject(requester, projectKey, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{projectKey}/tasks")
    public ResponseEntity<List<TaskResponseDto>> getAllTasksInProject(@AuthenticationPrincipal User requester,
                                                                      @PathVariable String projectKey) {
        List<TaskResponseDto> tasks = projectService.getAllTasksInProject(requester, projectKey);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{projectKey}/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsersInProject(@AuthenticationPrincipal User requester,
                                                                      @PathVariable String projectKey) {
        List<UserResponseDto> users = projectService.getAllUsersInProject(requester, projectKey);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{projectKey}/tasks/filter")
    public ResponseEntity<List<TaskResponseDto>> getFilteredTasksInProject(
            @AuthenticationPrincipal User requester,
            @PathVariable String projectKey,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long assignerId) {
        List<TaskResponseDto> tasks = projectService
                .getFilteredTasksInProject(requester, projectKey, creatorId, assignerId);
        return ResponseEntity.ok(tasks);
    }
}
