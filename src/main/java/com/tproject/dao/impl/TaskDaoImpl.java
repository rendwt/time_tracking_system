package com.tproject.dao.impl;

import com.tproject.dao.TaskDao;
import com.tproject.entity.Task;
import com.tproject.exception.CustomSQLException;
import com.tproject.exception.NonExistentEntityException;

import java.sql.*;
import java.time.LocalDate;
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
    public Optional<Task> findTaskById(int id) throws CustomSQLException {
        Optional<Task> task = Optional.empty();
        String sql = "SELECT * FROM task WHERE id = ?";
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = JdbcConnection.getInstance().getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);

            statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                task = Optional.of(composeTask(resultSet));
                LOGGER.log(Level.INFO, "Found {0} in database", task);
            }

            conn.commit();
        } catch (SQLException e) {
            handleSQLException(conn, "Error fetching task");
        } finally {
            closeResources(conn, statement, resultSet);
        }
        return task;
    }



    @Override
    public Collection<Task> getAllTasksFromUser(int userId) throws CustomSQLException {
        Collection<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = JdbcConnection.getInstance().getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);

            statement = conn.prepareStatement(sql);
            statement.setInt(1, userId);
            resultSet = statement.executeQuery();

            while (resultSet.next())
                tasks.add(composeTask(resultSet));

            conn.commit();
        } catch (SQLException e) {
            handleSQLException(conn, "Error fetching tasks for userId: " + userId);
        } finally {
            closeResources(conn, statement, resultSet);
        }
        return tasks;
    }

    @Override
    public Optional<Integer> saveTask(Task task) throws CustomSQLException {
        String sql = "INSERT INTO task (user_id, date, hours, description) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = JdbcConnection.getInstance().getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);

            statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, task.getUserId());
            statement.setDate(2, Date.valueOf(LocalDate.now()));
            statement.setFloat(3, task.getHours());
            statement.setString(4, task.getDescription());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                conn.commit();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int taskId = generatedKeys.getInt(1);
                    return Optional.of(taskId);
                }
            }

        } catch (SQLException e) {
            handleSQLException(conn, "Error saving task: " + e.getMessage());
        } finally {
            closeResources(conn, statement, null);
        }
        return Optional.empty();
    }

    @Override
    public Task updateTask(Task task) throws NonExistentEntityException, CustomSQLException {
        String sql = "UPDATE task SET date = ?, hours = ?, description = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = JdbcConnection.getInstance().getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);

            statement = conn.prepareStatement(sql);
            statement.setDate(1, Date.valueOf(task.getDate()));
            statement.setFloat(2, task.getHours());
            statement.setString(3, task.getDescription());
            statement.setInt(4, task.getId());

            int rowsUpdated = statement.executeUpdate();
            LOGGER.log(Level.INFO, "Updated {0} rows in database", rowsUpdated);

            if (rowsUpdated == 0) {
                throw new NonExistentEntityException("Failed to update task with ID: " + task.getId());
            }

            conn.commit();
        } catch (SQLException e) {
            handleSQLException(conn, "Error updating task: " + e.getMessage());
        } finally {
            closeResources(conn, statement, null);
        }
        return task;
    }

    @Override
    public boolean deleteTask(int id) throws CustomSQLException {
        String sql = "DELETE FROM task WHERE id = ?";
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = JdbcConnection.getInstance().getConnection();
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);

            statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            conn.commit();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            handleSQLException(conn, "Error deleting task: " + e.getMessage());
        } finally {
            closeResources(conn, statement, null);
        }
        return false;
    }

    public void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing ResultSet: " + e.getMessage());
        }

        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing PreparedStatement: " + e.getMessage());
        }

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing Connection: " + e.getMessage());
        }
    }

    private void handleSQLException(Connection conn, String message) {
        try {
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error during rollback: " + ex.getMessage());
        }
        LOGGER.log(Level.SEVERE, message);
        throw new CustomSQLException(message);
    }
}


