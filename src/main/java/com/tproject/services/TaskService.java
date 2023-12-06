package com.tproject.services;

import com.tproject.dto.TaskDto;
import com.tproject.dto.UserDto;
import com.tproject.exception.CustomSQLException;

import java.util.Collection;
import java.util.Optional;

public interface TaskService {

    <Optional> TaskDto findTaskById(int id) throws CustomSQLException;

    Collection<TaskDto> getAllTasks(int userId)  throws CustomSQLException;

    Optional<Integer> createTask(final TaskDto taskDto)  throws CustomSQLException;

    TaskDto updateTask(final TaskDto taskDto)  throws CustomSQLException;

    boolean deleteTask(int id)  throws CustomSQLException;
}

