package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.model.*;
import com.study.FlowTrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {
    private final TaskRepository taskRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    private static final String NOT_ENOUGH_RIGHTS = "Access denied: not enough rights.";

    public Project getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> resourceNotFound("Project", "id", id));
    }

    public Project getProjectEntityByKey(String key) {
        return projectRepository.findProjectByKey(key)
                .orElseThrow(() -> resourceNotFound("Project", "key", key));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public Task getTaskByProjectKeyAndNumber(String projectKey, Long taskNumber) {
        Project project = getProjectEntityByKey(projectKey);
        return taskRepository.findByProjectAndTaskNumber(project, taskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));
    }

    public void requireProjectNotViewer(ProjectMembership requesterMembership) {
        if (requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_VIEWER)) {
            throw new PermissionDeniedException(NOT_ENOUGH_RIGHTS);
        }
    }

    public ProjectMembership requireUserMembership(User requester, Project project) {
        return projectMembershipRepository.findByUserAndProject(requester, project).orElseThrow(
                () -> new PermissionDeniedException("Access denied: Must be a participant in the project")
        );
    }

    public void requireProjectAdminOrProductManager(ProjectMembership requesterMembership) {
        if (!requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN) &&
                !requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_PRODUCT_MANAGER)) {
            throw new PermissionDeniedException(NOT_ENOUGH_RIGHTS);
        }
    }

    public void requireProjectAdmin(ProjectMembership requesterMembership) {
        if (!requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN)) {
            throw new PermissionDeniedException(NOT_ENOUGH_RIGHTS);
        }
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> resourceNotFound("Comment", "id", id));
    }

    public void requireProjectAdminOrProductManagerOrCommentOwner(ProjectMembership requesterMembership,
                                                                   Comment comment) {
        boolean isAdminOrPM = requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN) ||
                requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_PRODUCT_MANAGER);

        boolean isOwner = requesterMembership.getUser().getId().equals(comment.getAuthor().getId());

        if (!isAdminOrPM && !isOwner) {
            throw new PermissionDeniedException(NOT_ENOUGH_RIGHTS);
        }
    }

    private ResourceNotFoundException resourceNotFound(String resource, String field, Object value) {
        return new ResourceNotFoundException(String.format("%s with %s: %s not found", resource, field, value));
    }


}
