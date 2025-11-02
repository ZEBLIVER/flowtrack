package com.study.FlowTrack.payload.comment;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentUpdateDto {
    private String newText;
}
