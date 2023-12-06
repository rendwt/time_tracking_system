package com.tproject.dao.impl;

import com.tproject.dao.UserDao;
import com.tproject.entity.User;
import com.tproject.exception.CustomSQLException;
import com.tproject.exception.NonExistentUserException;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserDaoImpl implements UserDao<User, Integer> {
    private static UserDaoImpl instance;


    public static UserDaoImpl getInstance() {
        UserDaoImpl localInstance = instance;
        if (localInstance == null) {
            synchronized (UserDaoImpl.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new UserDaoImpl();
                }
            }
        }
        return localInstance;
    }

    private static final Logger LOGGER =
            Logger.getLogger(UserDaoImpl.class.getName());

    private UserDaoImpl() {
    }

    private User composeUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        return user;
    }


    @Override
    public Optional<User> findUser(int id) throws CustomSQLException, NonExistentUserException{
        Optional<User> userOpt = Optional.empty();
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                userOpt = Optional.of(composeUser(resultSet));

                LOGGER.log(Level.INFO, "Found {0} in database", userOpt.get());
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("findUser - SQL Exception");
        }
        return userOpt;
    }

    @Override
    public Optional<User> findUser(String login)  throws CustomSQLException, NonExistentUserException{
        Optional<User> userOpt = Optional.empty();
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                userOpt = Optional.of(composeUser(resultSet));
                LOGGER.log(Level.INFO, "Found {0} in database", userOpt.get());
            }
            else{
                throw new NonExistentUserException();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("findUser - SQL Exception");
        }

        return userOpt;
    }

//    @Override
//    public Optional<Credentials> getCredentials(String login) {
////        return connection.flatMap(conn -> {
//        Optional<Credentials> credOpt = Optional.empty();
//        String sql = "SELECT password FROM users WHERE username = ?";
//
//        try (Connection conn = JdbcConnection.getInstance().getConnection();
//             PreparedStatement statement = conn.prepareStatement(sql)) {
//            statement.setString(1, login);
//
//            ResultSet resultSet = statement.executeQuery();
//
//            if (resultSet.next()) {
//                String password = resultSet.getString("password");
//                System.out.println(password);
//                credOpt = Optional.of(new Credentials(login, password));
//
//                LOGGER.log(Level.INFO, "Found {0} in database", credOpt.get());
//            }
//        } catch (SQLException ex) {
//            LOGGER.log(Level.SEVERE, null, ex);
//        }
//
//        return credOpt;
////        });
//    }

    @Override
    public Collection<User> getAll()  throws CustomSQLException{
        Collection<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                User user = composeUser(resultSet);

                users.add(user);

                LOGGER.log(Level.INFO, "Found {0} in database", user);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("getAll - SQL Exception");
        }

        return users;
    }

    @Override
    public Optional<Integer> saveUser(User User)  throws CustomSQLException{
        String message = "The User to be added should not be null";
        User nonNullUser = Objects.requireNonNull(User, message);
        String sql = "INSERT INTO "
                + "Users(username, password) "
                + "VALUES(?, ?)";
        Optional<Integer> generatedId = Optional.empty();

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement =
                     conn.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, nonNullUser.getUsername());
            statement.setString(2, DigestUtils.sha256Hex(nonNullUser.getPassword()));

            int numberOfInsertedRows = statement.executeUpdate();

            if (numberOfInsertedRows > 0) {
                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        generatedId = Optional.of(resultSet.getInt(1));
                    }
                }
            }

            LOGGER.log(Level.INFO, "{0} created successfully? {1}",
                    new Object[]{nonNullUser, (numberOfInsertedRows > 0)});
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("saveUser - SQL Exception");
        }

        return generatedId;
    }

    @Override
    public User updateUser(User user)  throws CustomSQLException{
        String message = "The user to be updated should not be null";
        User nonNullUser = Objects.requireNonNull(user, message);
        String sql = "UPDATE users "
                + "SET "
                + "username = ?, "
                + "password = ?, "
                + "WHERE "
                + "user_id = ?";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {


            statement.setString(1, nonNullUser.getUsername());
            statement.setString(2, DigestUtils.sha256Hex(nonNullUser.getPassword()));
            statement.setInt(3, nonNullUser.getId());

            int numberOfUpdatedRows = statement.executeUpdate();

            LOGGER.log(Level.INFO, "Was the customer updated successfully? {0}",
                    numberOfUpdatedRows > 0);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("updateUser - SQL Exception");
        }
        return user;
    }

    @Override
    public boolean deleteUser(int id)  throws CustomSQLException{
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = JdbcConnection.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, id);

            int numberOfDeletedRows = statement.executeUpdate();

            LOGGER.log(Level.INFO, "Was the customer deleted successfully? {0}",
                    numberOfDeletedRows > 0);

            return numberOfDeletedRows > 0;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new CustomSQLException("deleteUser - SQL Exception");
        }
    }

}
