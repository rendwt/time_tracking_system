package com.tproject.mappers;

import com.tproject.dto.UserDto;
import com.tproject.entity.User;
import org.mapstruct.Mapper;

import java.util.Collection;

@Mapper
public interface UserMapper {
    UserDto userToDto(User user);

    User dtoToUser(UserDto userDto);

    Collection<UserDto> userToDtoCollection (Collection<User> userCollection);

    Collection<User> dtoToUserCollection (Collection<UserDto> userDtoCollection);
}
