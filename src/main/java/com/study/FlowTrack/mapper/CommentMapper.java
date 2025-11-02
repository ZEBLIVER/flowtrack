package com.study.FlowTrack.mapper;

import com.study.FlowTrack.model.Comment;
import com.study.FlowTrack.payload.comment.CommentCreationDto;
import com.study.FlowTrack.payload.comment.CommentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    Comment toEntity(CommentCreationDto dto);

    @Mapping(target = "author", source = "author")
    @Mapping(target = "creationTime", source = "creationTime")
    CommentResponseDto toResponseDto(Comment comment);

    List<CommentResponseDto> toResponseListDto(List<Comment> comments);
}
