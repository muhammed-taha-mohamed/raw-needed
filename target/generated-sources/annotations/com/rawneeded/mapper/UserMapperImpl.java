package com.rawneeded.mapper;

import com.rawneeded.dto.user.CreateUserDto;
import com.rawneeded.dto.user.UserRequestDto;
import com.rawneeded.dto.user.UserResponseDto;
import com.rawneeded.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-06T12:03:50+0000",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 17.0.5 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(CreateUserDto dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setName( dto.getName() );
        user.setRole( dto.getRole() );
        user.setPassword( dto.getPassword() );
        user.setEmail( dto.getEmail() );
        user.setPhoneNumber( dto.getPhoneNumber() );
        user.setPreferredCategory( dto.getPreferredCategory() );

        return user;
    }

    @Override
    public void update(User user, UserRequestDto requestDTO) {
        if ( requestDTO == null ) {
            return;
        }

        if ( requestDTO.getRole() != null ) {
            user.setRole( requestDTO.getRole() );
        }
        if ( requestDTO.getEmail() != null ) {
            user.setEmail( requestDTO.getEmail() );
        }
        if ( requestDTO.getPhoneNumber() != null ) {
            user.setPhoneNumber( requestDTO.getPhoneNumber() );
        }
    }

    @Override
    public UserResponseDto toResponseDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDto userResponseDto = new UserResponseDto();

        userResponseDto.setId( user.getId() );
        userResponseDto.setName( user.getName() );
        userResponseDto.setRole( user.getRole() );
        userResponseDto.setEmail( user.getEmail() );
        userResponseDto.setPhoneNumber( user.getPhoneNumber() );
        userResponseDto.setPreferredCategory( user.getPreferredCategory() );

        return userResponseDto;
    }
}
