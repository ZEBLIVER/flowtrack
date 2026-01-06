package com.study.FlowTrack.service;

import com.study.FlowTrack.exception.PermissionDeniedException;
import com.study.FlowTrack.exception.ResourceNotFoundException;
import com.study.FlowTrack.mapper.CommentMapper;
import com.study.FlowTrack.model.*;
import com.study.FlowTrack.payload.comment.CommentCreationDto;
import com.study.FlowTrack.payload.comment.CommentResponseDto;
import com.study.FlowTrack.payload.comment.CommentUpdateDto;
import com.study.FlowTrack.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    private User requester;
    private String projectKey;
    private Long taskNumber;
    private Project project;
    private Task task;
    private Comment comment;

    @BeforeEach
    void setUp() {
        requester = new User();
        projectKey = "key";
        taskNumber = 1L;
        project = new Project();
        task = new Task();
        comment = new Comment();
    }

    @Test
    void createComment_Success() {
        CommentCreationDto dto = new CommentCreationDto();
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
        CommentCreationDto dto = new CommentCreationDto();

        when(projectAccessService.getProjectEntityByKey(projectKey))
                .thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(requester, projectKey, taskNumber, dto));
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }


    @Test
    void getCommentsByTask_Success() {
        List<Comment> comments = List.of(new Comment());

        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(123L);
        List<CommentResponseDto> commentsDto = List.of(dto);

        when(projectAccessService.getProjectEntityByKey(projectKey)).thenReturn(project);
        when(projectAccessService.requireUserMembership(requester, project)).thenReturn(new ProjectMembership());
        when(projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber)).thenReturn(task);
        when(commentRepository.findAllByTask(task)).thenReturn(comments);
        when(commentMapper.toResponseListDto(comments)).thenReturn(commentsDto);

        List<CommentResponseDto> commentsResponseDto = commentService.getCommentsByTask(requester, projectKey, taskNumber);

        assertNotNull(commentsResponseDto);
        assertEquals(1, commentsResponseDto.size());
        assertEquals(123L, commentsResponseDto.get(0).getId());
        verify(commentRepository).findAllByTask(task);
    }

    @Test
    void getCommentsByTask_AccessDenied_ThrowsException() {
        when(projectAccessService.getProjectEntityByKey(projectKey)).thenReturn(project);
        when(projectAccessService.requireUserMembership(requester, project)).thenThrow(new PermissionDeniedException("Access Denied"));

        assertThrows(PermissionDeniedException.class, () -> commentService.getCommentsByTask(requester, projectKey, taskNumber));
        verifyNoInteractions(commentMapper);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void updateComment_Success() {
        Long commentId = 3L;

        CommentUpdateDto updateDto = new CommentUpdateDto();
        updateDto.setNewText("sun");

        task.setId(22L);
        task.setProject(project);

        comment.setTask(task);
        comment.setCommentText("moon");


        when(projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber)).thenReturn(task);
        when(projectAccessService.getCommentById(commentId)).thenReturn(comment);
        when(projectAccessService.requireUserMembership(requester, project)).thenReturn(new ProjectMembership());
        when(commentRepository.save(comment)).thenReturn(comment);

        when(commentMapper.toResponseDto(comment)).thenAnswer(invocation -> {
            Comment arg = invocation.getArgument(0);
            CommentResponseDto dto = new CommentResponseDto();
            dto.setCommentText(arg.getCommentText());
            return dto;
        });

        CommentResponseDto responseDto = commentService.updateComment(requester, projectKey, taskNumber, commentId, updateDto);

        assertNotNull(responseDto);
        assertEquals("sun", comment.getCommentText());
        assertEquals("sun", responseDto.getCommentText());
        verify(commentRepository).save(comment);
    }

    @Test
    void updateComment_WithNullText_DoesNotUpdateField() {
        Long commentId = 3L;

        CommentUpdateDto updateDto = new CommentUpdateDto();
        updateDto.setNewText(null);

        task.setId(22L);
        task.setProject(project);

        comment.setTask(task);
        comment.setCommentText("moon");


        when(projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber)).thenReturn(task);
        when(projectAccessService.getCommentById(commentId)).thenReturn(comment);
        when(projectAccessService.requireUserMembership(requester, project)).thenReturn(new ProjectMembership());
        when(commentRepository.save(comment)).thenReturn(comment);

        when(commentMapper.toResponseDto(comment)).thenAnswer(invocation -> {
            Comment arg = invocation.getArgument(0);
            CommentResponseDto dto = new CommentResponseDto();
            dto.setCommentText(arg.getCommentText());
            return dto;
        });

        CommentResponseDto responseDto = commentService.updateComment(requester, projectKey, taskNumber, commentId, updateDto);

        assertNotNull(responseDto);
        assertEquals("moon", comment.getCommentText());
        assertEquals("moon", responseDto.getCommentText());
        verify(commentRepository).save(comment);
    }


    @Test
    void updateComment_TaskNotFound_ThrowsException() {
        CommentUpdateDto updateDto = new CommentUpdateDto();
        updateDto.setNewText("sun");
        Long commentId = 1L;

        when(projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber)).thenThrow(new ResourceNotFoundException());
        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(requester, projectKey, taskNumber, commentId, updateDto));

        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }

    @Test
    void updateComment_CommentNotFound_ThrowsException() {
        CommentUpdateDto updateDto = new CommentUpdateDto();
        Long commentId = 1L;

        when(projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber)).thenReturn(task);
        when(projectAccessService.getCommentById(commentId)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(requester, projectKey, taskNumber, commentId, updateDto));

        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }

    @Test
    void updateComment_CommentTaskMismatch_ThrowsException() {
        CommentUpdateDto updateDto = new CommentUpdateDto();
        Long commentId = 1L;

        Task wrongTask = new Task();
        wrongTask.setId(999L);
        wrongTask.setProject(project);

        comment.setTask(wrongTask);

        when(projectAccessService.getTaskByProjectKeyAndNumber(projectKey, taskNumber)).thenReturn(task);
        when(projectAccessService.getCommentById(commentId)).thenReturn(comment);
        when(projectAccessService.requireUserMembership(requester, project)).thenReturn(new ProjectMembership());

        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(requester, projectKey, taskNumber, commentId, updateDto));

        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }


    @Test
    void deleteComment_Success() {
        Long commentId = 1L;

        task.setProject(project);

        comment.setTask(task);

        when(projectAccessService.getCommentById(commentId)).thenReturn(comment);
        when(projectAccessService.requireUserMembership(requester, project)).thenReturn(new ProjectMembership());

        commentService.deleteComment(requester, commentId);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_CommentNotFound_ThrowsException() {
        Long commentId = 1L;

        task.setProject(project);

        comment.setTask(task);

        when(projectAccessService.getCommentById(commentId)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(requester, commentId));

        verifyNoInteractions(commentRepository);
    }

    @Test
    void deleteComment_ProjectMembershipNotFound_ThrowsException() {
        Long commentId = 1L;

        task.setProject(project);

        comment.setTask(task);

        when(projectAccessService.getCommentById(commentId)).thenReturn(comment);
        when(projectAccessService.requireUserMembership(requester, project)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(requester, commentId));

        verifyNoInteractions(commentRepository);
    }
}