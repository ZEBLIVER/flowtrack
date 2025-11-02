package com.study.FlowTrack.payload.comment;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentCreationDto {
    private String commentText;
}
