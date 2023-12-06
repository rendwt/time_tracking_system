package com.tproject.dao;

import com.tproject.entity.Task;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

public interface TaskDao {

    Optional<Task> findTaskById(int id) throws SQLException;
    Collection<Task> getAllTasksFromUser(String username);
    Optional<Integer> saveTask(Task task);
    Task updateTask(Task task);
    boolean deleteTask(int id);
}
