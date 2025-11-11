package com.study.FlowTrack.mapper;

import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.user.UserResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface UserMapper {
    UserResponseDto toResponseDto(User user);
}
