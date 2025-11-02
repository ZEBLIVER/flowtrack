package com.study.FlowTrack.service;

import com.study.FlowTrack.enums.ProjectRole;
import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.CommentMapper;
import com.study.FlowTrack.model.*;
import com.study.FlowTrack.payload.comment.CommentCreationDto;
import com.study.FlowTrack.payload.comment.CommentResponseDto;
import com.study.FlowTrack.payload.comment.CommentUpdateDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.repository.CommentRepository;
import com.study.FlowTrack.repository.ProjectMembershipRepository;
import com.study.FlowTrack.repository.ProjectRepository;
import com.study.FlowTrack.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final TaskRepository taskRepository;

    public CommentResponseDto createComment(User requester, String projectKey,
                                            Long taskNumber, CommentCreationDto dto) {
        Project project = getProjectEntityByKey(projectKey);
        ProjectMembership membership = requireUserMembership(requester, project);
        requireProjectNotViewer(membership);

        Task task = getTaskByProjectKeyAndNumber(projectKey, taskNumber);

        Comment comment = commentMapper.toEntity(dto);
        comment.setAuthor(requester);
        comment.setTask(task);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponseDto(savedComment);
    }

    public List<CommentResponseDto> getCommentsByTask(User requester, String projectKey, Long taskNumber) {
        Project project = getProjectEntityByKey(projectKey);
        ProjectMembership membership = requireUserMembership(requester,project);
        requireProjectNotViewer(membership);
        Task task = getTaskByProjectKeyAndNumber(projectKey,taskNumber);
        List<Comment> comments = commentRepository.findAllByTask(task);
        return commentMapper.toResponseListDto(comments);
    }

    public CommentResponseDto updateComment(User requester, String projectKey,
                                            Long taskNumber, Long commentId, CommentUpdateDto dto) {
        Task task = getTaskByProjectKeyAndNumber(projectKey, taskNumber);
        Comment comment = getCommentById(commentId);
        Project project = comment.getTask().getProject();
        ProjectMembership projectMembership = requireUserMembership(requester,project);
        requireProjectAdminOrProductManagerOrCommentOwner(projectMembership,comment);
        if (!comment.getTask().getId().equals(task.getId())) {
            throw new ResourceNotFoundException("Comment ID " + commentId + " is not attached to task " + projectKey + "-" + taskNumber);
        }
        if (dto.getNewText() != null) comment.setCommentText(dto.getNewText());
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponseDto(savedComment);
    }

    public void deleteComment(User requester, Long commentId) {
        Comment comment = getCommentById(commentId);
        Project project = comment.getTask().getProject();
        ProjectMembership projectMembership = requireUserMembership(requester,project);
        requireProjectAdminOrProductManagerOrCommentOwner(projectMembership,comment);
        commentRepository.delete(comment);
    }


    private void requireProjectAdminOrProductManagerOrCommentOwner(ProjectMembership requesterMembership,
                                                                   Comment comment) {
        boolean isAdminOrPM = requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_ADMIN) ||
                requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_PRODUCT_MANAGER);

        boolean isOwner = requesterMembership.getUser().getId().equals(comment.getAuthor().getId());

        if (!isAdminOrPM && !isOwner) {
            throw new PermissionDeniedException("Access denied: not enough rights.");
        }
    }

    private Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Comment with id: " + id + " not found"));
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

    private void requireProjectNotViewer(ProjectMembership requesterMembership) {
        if (requesterMembership.getProjectRole().equals(ProjectRole.PROJECT_VIEWER)) {
            throw new PermissionDeniedException("Access denied: not enough rights.");
        }
    }

    private Task getTaskByProjectKeyAndNumber(String projectKey, Long taskNumber) {
        Project project = getProjectEntityByKey(projectKey);
        return taskRepository.findByProjectAndTaskNumber(project, taskNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found."));
    }
}
