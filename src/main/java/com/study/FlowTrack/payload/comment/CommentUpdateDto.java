package com.study.FlowTrack.payload.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentUpdateDto {
    private String newText;
}
