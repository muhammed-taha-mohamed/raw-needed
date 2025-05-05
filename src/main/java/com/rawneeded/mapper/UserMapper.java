package com.rawneeded.mapper;

import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.model.User;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring" )
public interface UserMapper {



    User toEntity(CreateUserDto dto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)

    void update(@MappingTarget User user, UserRequestDto requestDTO);

    UserResponseDto toResponseDto(User user);

    default Page<UserResponseDto> toResponsePages(Page<User> users){
        return users.map(this::toResponseDto);
    }

}
