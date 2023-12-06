package com.tproject.dao.impl;

import com.tproject.dao.TaskDao;
import com.tproject.entity.Task;
import com.tproject.exception.CustomSQLException;
import com.tproject.exception.NonExistentEntityException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskDaoImpl implements TaskDao{
    private static TaskDaoImpl instance;

    public static TaskDaoImpl getInstance() {
        TaskDaoImpl localInstance = instance;
        if (localInstance == null) {
            synchronized (TaskDaoImpl.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TaskDaoImpl();
                }
            }
        }
        return localInstance;
    }

    private static final Logger LOGGER =
            Logger.getLogger(TaskDaoImpl.class.getName());

    private Task composeTask(ResultSet resultSet) throws SQLException {
        Task task = new Task();
        task.setId(resultSet.getInt("id"));
        task.setDescription(resultSet.getString("description"));
        task.setDate(resultSet.getDate("date").toLocalDate());
        task.setHours(resultSet.getFloat("hours"));
        task.setUserId(resultSet.getInt("user_id"));
        return task;
    }

    @Override
    public Optional<Task> findTaskById(int id) throws CustomSQLException{
        Optional<Task> task = Optional.empty();
        String sql = "SELECT * FROM task WHERE id = ?";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                task = Optional.of(composeTask(resultSet));
                LOGGER.log(Level.INFO, "Found {0} in database", task);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("Error fetching task with id: " + id);
        }
        return task;
    }



    @Override
    public Collection<Task> getAllTasksFromUser(int userId) throws CustomSQLException{
        Collection<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE user_id = ?";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                tasks.add(composeTask(resultSet));
                LOGGER.log(Level.INFO, "Found {0} in database", composeTask(resultSet));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("Error fetching tasks for user: " + userId);
        }
        return tasks;
    }

    @Override
    public Optional<Integer> saveTask(Task task) throws CustomSQLException{
        String sql = "INSERT INTO task (user_id, date, hours, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, task.getUserId());
            statement.setDate(2, Date.valueOf(task.getDate()));
            statement.setFloat(3, task.getHours());
            statement.setString(4, task.getDescription());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int taskId = generatedKeys.getInt(1);
                    return Optional.of(taskId);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("Error saving task: " + ex.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Task updateTask(Task task) throws NonExistentEntityException, CustomSQLException{
        String sql = "UPDATE task SET date = ?, hours = ?, description = ? WHERE id = ?";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(task.getDate()));
            statement.setFloat(2, task.getHours());
            statement.setString(3, task.getDescription());
            statement.setInt(4, task.getId());

            int rowsUpdated = statement.executeUpdate();
            LOGGER.log(Level.INFO, "Updated {0} rows in database", rowsUpdated);

            if (rowsUpdated == 0) {
                throw new NonExistentEntityException("Failed to update task with ID: " + task.getId());
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("Error updating task: " + ex.getMessage());
        }
        return task;
    }

    @Override
    public boolean deleteTask(int id) throws CustomSQLException{
        String sql = "DELETE FROM task WHERE id = ?";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("Error deleting task: " + ex.getMessage());
        }
    }

}


