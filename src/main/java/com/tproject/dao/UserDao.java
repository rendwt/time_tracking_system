package com.tproject.dao;

import com.tproject.entity.User;
import com.tproject.exception.NonExistentUserException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public interface UserDao<T, I> {
    Optional<T> getUserIdByUsername(String username) throws SQLException, NonExistentUserException;
    Optional<T> findUser(String login) throws SQLException, NonExistentUserException;
    //Collection<T> getAll();
    Optional<I> saveUser(T t);
    //User updateUser(T t);
    //boolean deleteUser(int id);
}
