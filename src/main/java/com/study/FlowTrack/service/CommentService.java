package com.study.FlowTrack.service;

import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.CommentMapper;
import com.study.FlowTrack.model.*;
import com.study.FlowTrack.payload.comment.CommentCreationDto;
import com.study.FlowTrack.payload.comment.CommentResponseDto;
import com.study.FlowTrack.payload.comment.CommentUpdateDto;
import com.study.FlowTrack.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ProjectAccessService projectAccessService;

    @CacheEvict(value = "comments", key = "#projectKey + '_' + #taskNumber")
    public CommentResponseDto createComment(User requester, String projectKey,
                                            Long taskNumber, CommentCreationDto dto) {
        Project project = projectAccessService.getProjectEntityByKey(projectKey);
        ProjectMembership membership = projectAccessService.requireUserMembership(requester, project);
        projectAccessService.requireProjectNotViewer(membership);

        Task task = projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber);

        Comment comment = commentMapper.toEntity(dto);
        comment.setAuthor(requester);
        comment.setTask(task);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponseDto(savedComment);
    }

    @Cacheable(value = "comments", key = "#projectKey + '_' + #taskNumber")
    public List<CommentResponseDto> getCommentsByTask(User requester, String projectKey, Long taskNumber) {
        Project project = projectAccessService.getProjectEntityByKey(projectKey);
        ProjectMembership membership = projectAccessService.requireUserMembership(requester,project);
        projectAccessService.requireProjectNotViewer(membership);
        Task task = projectAccessService.getTaskByProjectKeyAndNumber(projectKey,taskNumber);
        List<Comment> comments = commentRepository.findAllByTask(task);
        return commentMapper.toResponseListDto(comments);
    }

    @CacheEvict(value = "comments", key = "#projectKey + '_' + #taskNumber")
    public CommentResponseDto updateComment(User requester, String projectKey,
                                            Long taskNumber, Long commentId, CommentUpdateDto dto) {
        Task task = projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber);
        Comment comment = projectAccessService.getCommentById(commentId);
        Project project = comment.getTask().getProject();
        ProjectMembership projectMembership = projectAccessService.requireUserMembership(requester,project);
        projectAccessService.requireProjectAdminOrProductManagerOrCommentOwner(projectMembership,comment);
        if (!comment.getTask().getId().equals(task.getId())) {
            throw new ResourceNotFoundException("Comment ID " + commentId + " is not attached to task " + projectKey + "-" + taskNumber);
        }
        if (dto.getNewText() != null) comment.setCommentText(dto.getNewText());
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponseDto(savedComment);
    }

    @CacheEvict(value = "comments", allEntries = true)
    public void deleteComment(User requester, Long commentId) {
        Comment comment = projectAccessService.getCommentById(commentId);
        Project project = comment.getTask().getProject();
        ProjectMembership projectMembership = projectAccessService.requireUserMembership(requester,project);
        projectAccessService.requireProjectAdminOrProductManagerOrCommentOwner(projectMembership,comment);
        commentRepository.delete(comment);
    }

}
