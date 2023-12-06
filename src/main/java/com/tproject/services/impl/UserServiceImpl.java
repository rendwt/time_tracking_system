package com.tproject.services.impl;

import com.tproject.dao.impl.UserDaoImpl;
import com.tproject.dto.UserDto;
import com.tproject.entity.User;
import com.tproject.exception.CustomSQLException;
import com.tproject.mappers.UserMapper;
import com.tproject.services.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private UserDaoImpl userDao;
    private UserMapper mapper;

    private static UserServiceImpl instance;

    private UserServiceImpl() {
        userDao = UserDaoImpl.getInstance();
        mapper = Mappers.getMapper(UserMapper.class);
    }


    public static UserServiceImpl getInstance() {
        UserServiceImpl localInstance = instance;
        if (localInstance == null) {
            synchronized (UserServiceImpl.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new UserServiceImpl();
                }
            }
        }
        return localInstance;
    }

//    public static UserServiceImpl getInstance() {
//        if (instance == null) {
//            instance = new UserServiceImpl();
//        }
//        return instance;
//    }


    public UserDto findUserById(int id) throws RuntimeException{
        try {
            return mapper.userToDto(userDao.findUser(id).get());
        } catch (RuntimeException e) {
            throw e;
        }
    }


    public UserDto findUserByUsername(String username) throws CustomSQLException {
        try {
            return mapper.userToDto(userDao.findUser(username).get());
        } catch (CustomSQLException e) {
            throw e;
        }
    }


    public Collection<UserDto> getAllUsers() throws CustomSQLException {
        try {
            return mapper.userToDtoCollection(userDao.getAll());
        } catch (CustomSQLException e) {
            throw e;
        }
    }


//    public Collection<UserDto> getAllUsersFromCompany(String company) {
//        return mapper.userToDtoCollection(userDao.getAllFromCompany(company));
//    }

    public Optional<Integer> createUser(final UserDto userDto) throws CustomSQLException {
        try {
            User user = mapper.dtoToUser(userDto);
            return userDao.saveUser(user);
        } catch (CustomSQLException e) {
            throw e;
        }
    }


    public UserDto updateUser(final UserDto userDto) throws CustomSQLException {
        try {
            return mapper.userToDto(
                    userDao.updateUser(mapper.dtoToUser(userDto)));
        } catch (CustomSQLException e) {
            throw e;
        }
    }

    public boolean deleteUser(int id) throws CustomSQLException {
        try {
            return userDao.deleteUser(id);
        } catch (CustomSQLException e) {
            throw e;
        }
    }

    public boolean verifyUser(String login, String password){
        Optional<User> optionalUser = userDao.findUser(login);
        if (optionalUser.isPresent()) {
            System.out.println("PASSWORD SENT:===============================" + password);
            System.out.println("PASSWORD STORED:=============================" + optionalUser.get().getPassword());
            System.out.println("PASSWORD ENCRYPTED:==========================" + DigestUtils.sha256Hex(password));
            return DigestUtils.sha256Hex(password).equals(optionalUser.get().getPassword());
        } else {
            return false;
        }

    }
}
