package com.study.FlowTrack.payload.comment;

import com.study.FlowTrack.payload.user.UserResponseDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String commentText;
    private LocalDateTime creationTime;
    private UserResponseDto author;

}
