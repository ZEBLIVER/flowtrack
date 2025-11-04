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

//    public boolean isUserMember(String projectKey, String username) {
//        User user = userRepository.findByUserName(username).orElse(null);
//        if (user == null) return false;
//
//        Project project = projectRepository.findProjectByKey(projectKey).orElse(null);
//        if (project == null) return false;
//
//        return projectMembershipRepository.existsByUserAndProject(user,project);
//    }
//
//    public boolean isUserMember(Long projectId, String username) {
//        User user = userRepository.findByUserName(username).orElse(null);
//        if (user == null) return false;
//
//        Project project = projectRepository.findProjectById(projectId).orElse(null);
//        if (project == null) return false;
//
//        return projectMembershipRepository.existsByUserAndProject(user,project);
//    }

    public Project getProjectEntityById(Long id) {
        return projectRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Project with id: " + id + " not found")
        );
    }

    public Project getProjectEntityByKey(String key) {
        return projectRepository.findProjectByKey(key).orElseThrow(
                () -> new ResourceNotFoundException("Project with key: " + key + " not found")
        );
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
            throw new PermissionDeniedException("Access denied: not enough rights.");
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
            throw new PermissionDeniedException("Access denied: not enough rights.");
        }
    }

    public void requireProjectAdmin(ProjectMembership requesterMembership) {
        if (!requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN)) {
            throw new PermissionDeniedException("Access denied: not enough rights.");
        }
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Comment with id: " + id + " not found"));
    }


    public void requireProjectAdminOrProductManagerOrCommentOwner(ProjectMembership requesterMembership,
                                                                   Comment comment) {
        boolean isAdminOrPM = requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN) ||
                requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_PRODUCT_MANAGER);

        boolean isOwner = requesterMembership.getUser().getId().equals(comment.getAuthor().getId());

        if (!isAdminOrPM && !isOwner) {
            throw new PermissionDeniedException("Access denied: not enough rights.");
        }
    }


}
