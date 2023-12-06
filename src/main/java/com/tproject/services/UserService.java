package com.tproject.services;

import com.tproject.dto.UserDto;
import com.tproject.exception.CustomSQLException;

import java.util.Collection;
import java.util.Optional;

public interface UserService {

    <Optional> UserDto findUserById(int id) throws CustomSQLException;

    <Optional>UserDto findUserByUsername(String username)  throws CustomSQLException;

    Collection<UserDto> getAllUsers()  throws CustomSQLException;

    Optional<Integer> createUser(final UserDto userDto)  throws CustomSQLException;

    UserDto updateUser(final UserDto userDto)  throws CustomSQLException;

    boolean deleteUser(int id)  throws CustomSQLException;
}
