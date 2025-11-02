package com.study.FlowTrack.controller;

import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.comment.CommentCreationDto;
import com.study.FlowTrack.payload.comment.CommentResponseDto;
import com.study.FlowTrack.payload.comment.CommentUpdateDto;
import com.study.FlowTrack.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{projectKey}/{taskNumber}/comment")
    public ResponseEntity<CommentResponseDto> createComment(
            @AuthenticationPrincipal User requester,
            @PathVariable String projectKey,
            @PathVariable Long taskNumber,
            @RequestBody CommentCreationDto dto) {

        CommentResponseDto creationDto = commentService.createComment(requester, projectKey, taskNumber, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{commentId}")
                .buildAndExpand(creationDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(creationDto);
    }

    @GetMapping("/{projectKey}/{taskNumber}/comment")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByTask(
            @AuthenticationPrincipal User requester,
            @PathVariable String projectKey,
            @PathVariable Long taskNumber) {

        List<CommentResponseDto> comments = commentService.getCommentsByTask(requester, projectKey, taskNumber);
        return ResponseEntity.ok(comments);
    }

    @PatchMapping("/{projectKey}/{taskNumber}/comment/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @AuthenticationPrincipal User requester,
            @PathVariable String projectKey,
            @PathVariable Long taskNumber,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateDto dto) {

        CommentResponseDto updatedComment = commentService
                .updateComment(requester, projectKey, taskNumber, commentId, dto);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{projectKey}/{taskNumber}/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal User requester,
            @PathVariable String projectKey,
            @PathVariable Long taskNumber,
            @PathVariable Long commentId) {

        commentService.deleteComment(requester, commentId);
        return ResponseEntity.noContent().build();
    }

}
