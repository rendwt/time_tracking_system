package com.tproject.services.impl;

import com.tproject.dao.impl.TaskDaoImpl;
import com.tproject.dto.TaskDto;
import com.tproject.entity.Task;
import com.tproject.exception.CustomSQLException;
import com.tproject.mappers.TaskMapper;
import com.tproject.services.TaskService;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.Optional;

public class TaskServiceImpl implements TaskService {
    private TaskDaoImpl taskDao;
    private TaskMapper mapper;

    private static TaskServiceImpl instance;

    private TaskServiceImpl() {
        taskDao = TaskDaoImpl.getInstance();
        mapper = Mappers.getMapper(TaskMapper.class);
    }

    public static TaskServiceImpl getInstance() {
        TaskServiceImpl localInstance = instance;
        if (localInstance == null) {
            synchronized (TaskServiceImpl.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TaskServiceImpl();
                }
            }
        }
        return localInstance;
    }

    public TaskDto getTaskById(int id) throws CustomSQLException {
        try {
            return mapper.taskToDto(taskDao.findTaskById(id).orElseThrow(() -> new CustomSQLException("Task not found")));
        } catch (CustomSQLException e) {
            throw e;
        }
    }

    public Collection<TaskDto> getAllTasks(int userId) throws CustomSQLException {
        try {
            return mapper.taskToDtoCollection(taskDao.getAllTasksFromUser(userId));
        } catch (CustomSQLException e) {
            throw e;
        }
    }

    public Optional<Integer> createTask(final TaskDto taskDto) throws CustomSQLException {
        try {
            Task task = mapper.dtoToTask(taskDto);
            return taskDao.saveTask(task);
        } catch (CustomSQLException e) {
            throw e;
        }
    }

    public TaskDto updateTask(final TaskDto taskDto) throws CustomSQLException {
        try {
            return mapper.taskToDto(
                    taskDao.updateTask(mapper.dtoToTask(taskDto)));
        } catch (CustomSQLException e) {
            throw e;
        }
    }

    public boolean deleteTask(int id) throws CustomSQLException {
        try {
            return taskDao.deleteTask(id);
        } catch (CustomSQLException e) {
            throw e;
        }
    }
}
