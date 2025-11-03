package com.study.FlowTrack.payload.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentCreationDto {
    @NotBlank(message = "Comment text is required")
    private String commentText;
}
