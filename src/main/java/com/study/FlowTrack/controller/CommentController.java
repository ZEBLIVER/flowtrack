package com.study.FlowTrack.controller;

import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.comment.CommentCreationDto;
import com.study.FlowTrack.payload.comment.CommentResponseDto;
import com.study.FlowTrack.payload.comment.CommentUpdateDto;
import com.study.FlowTrack.service.CommentService;
import com.study.FlowTrack.util.TaskIdentifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{taskIdentifier}/comment")
    public ResponseEntity<CommentResponseDto> createComment(
            @AuthenticationPrincipal User requester,
            @PathVariable TaskIdentifier taskIdentifier,
            @RequestBody @Validated CommentCreationDto dto) {

        CommentResponseDto creationDto = commentService.createComment(requester,
                taskIdentifier.getProjectKey(), taskIdentifier.getTaskNumber(), dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{commentId}")
                .buildAndExpand(creationDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(creationDto);
    }

    @GetMapping("/{taskIdentifier}/comment")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByTask(
            @AuthenticationPrincipal User requester,
            @PathVariable TaskIdentifier taskIdentifier) {

        List<CommentResponseDto> comments = commentService.getCommentsByTask(requester,
                taskIdentifier.getProjectKey(), taskIdentifier.getTaskNumber());
        return ResponseEntity.ok(comments);
    }

    @PatchMapping("/{taskIdentifier}/comment/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @AuthenticationPrincipal User requester,
            @PathVariable TaskIdentifier taskIdentifier,
            @PathVariable Long commentId,
            @RequestBody @Validated CommentUpdateDto dto) {

        CommentResponseDto updatedComment = commentService.updateComment(
                requester, taskIdentifier.getProjectKey(), taskIdentifier.getTaskNumber(), commentId, dto);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{taskIdentifier}/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal User requester,
            @PathVariable TaskIdentifier taskIdentifier,
            @PathVariable Long commentId) {

        commentService.deleteComment(requester, commentId);
        return ResponseEntity.noContent().build();
    }

}
