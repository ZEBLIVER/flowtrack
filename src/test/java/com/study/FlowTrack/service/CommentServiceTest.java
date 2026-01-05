package com.study.FlowTrack.service;

import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.CommentMapper;
import com.study.FlowTrack.model.*;
import com.study.FlowTrack.payload.comment.CommentCreationDto;
import com.study.FlowTrack.payload.comment.CommentResponseDto;
import com.study.FlowTrack.payload.comment.CommentUpdateDto;
import com.study.FlowTrack.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ProjectAccessService projectAccessService;

    @Test
    void createComment_Success() {
        User requester = new User();
        String projectKey = "key";
        Long taskNumber = 1L;

        Project project = new Project();
        CommentCreationDto dto = new CommentCreationDto();
        Comment comment = new Comment();
        Task task = new Task();
        CommentResponseDto expectedResponse = new CommentResponseDto();

        when(projectAccessService.getProjectEntityByKey(projectKey)).thenReturn(project);
        when(projectAccessService.requireUserMembership(requester, project)).thenReturn(new ProjectMembership());
        when(projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber)).thenReturn(task);
        when(commentMapper.toEntity(dto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toResponseDto(comment)).thenReturn(expectedResponse);

        CommentResponseDto commentResponseDto = commentService.createComment(requester, projectKey, taskNumber, dto);

        assertNotNull(commentResponseDto);
        assertEquals(requester, comment.getAuthor());
        assertEquals(task, comment.getTask());

        verify(commentRepository).save(any(Comment.class));
    }


    @Test
    void createComment_ProjectNotFound_ThrowsException() {
        User requester = new User();
        String projectKey = "bad_key";
        Long taskNumber = 1L;

        CommentCreationDto dto = new CommentCreationDto();

        when(projectAccessService.getProjectEntityByKey(projectKey))
                .thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(requester, projectKey, taskNumber, dto));
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }


    @Test
    void getCommentsByTask_Success() {
        User requester = new User();
        String projectKey = "key";
        Long taskNumber = 2L;
        Project project = new Project();
        Task task = new Task();

        List<Comment> comments = List.of(new Comment());

        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(123L);
        List<CommentResponseDto> commentsDto = List.of(dto);

        when(projectAccessService.getProjectEntityByKey(projectKey)).thenReturn(project);
        when(projectAccessService.requireUserMembership(requester, project)).thenReturn(new ProjectMembership());
        when(projectAccessService.getTaskByProjectKeyAndNumber(projectKey,taskNumber)).thenReturn(task);
        when(commentRepository.findAllByTask(task)).thenReturn(comments);
        when(commentMapper.toResponseListDto(comments)).thenReturn(commentsDto);

        List<CommentResponseDto> commentsResponseDto = commentService.getCommentsByTask(requester,projectKey,taskNumber);

        assertNotNull(commentsResponseDto);
        assertEquals(1, commentsResponseDto.size());
        assertEquals(123L, commentsResponseDto.get(0).getId());
        verify(commentRepository).findAllByTask(task);
    }

    @Test
    void getCommentsByTask_AccessDenied_ThrowsException() {
        User requester = new User();
        String projectKey = "key";
        Long taskNumber = 2L;
        Project project = new Project();


        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(123L);

        when(projectAccessService.getProjectEntityByKey(projectKey)).thenReturn(project);
        when(projectAccessService.requireUserMembership(requester, project)).thenThrow(new PermissionDeniedException("Access Denied"));

        assertThrows(PermissionDeniedException.class, () -> commentService.getCommentsByTask(requester,projectKey,taskNumber));
        verifyNoInteractions(commentMapper);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void updateComment() {
    }

    @Test
    void deleteComment() {
    }
}