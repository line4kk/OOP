package dao;

import exceptions.DAOException;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO implements DAO<User> {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    @Override
    public void insert(User user) {
        logger.info("Вставка строки в таблицу users: {}", user);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO users (username, password_hash, role, factory_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getFactoryType());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при вставке данных в базу данных user", e);
            throw new DAOException("Ошибка при вставке данных в базу данных user", e);
        }
    }

    @Override
    public void deleteById(long id) {
        logger.info("Удаление строки из таблицы users: id = {}", id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при удалении строки из таблицы users: id = {}", id, e);
            throw new DAOException("Ошибка при удалении данных из базы данных user", e);
        }
    }

    public User select(String username) {
        logger.info("Получение строки User (без пароля) из таблицы users по username={}", username);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT id, username, role, factory_type FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(
                            resultSet.getLong("id"),
                            resultSet.getString("username"),
                            null,
                            resultSet.getString("role"),
                            resultSet.getString("factory_type")
                    );
                    logger.info("Успешно получена строка: {}", user);
                    return user;
                }
                else {
                    logger.info("Пользователь {} не найден", username);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении строки User (без пароля) пользователя {}", username, e);
            throw new DAOException("Ошибка при считывании данных из базы данных user", e);
        }
    }

    public User selectCredentials(String username) {
        logger.info("Получение id, username, password_hash из таблицы users по username={}", username);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(
                            resultSet.getLong("id"),
                            username,
                            resultSet.getString("password_hash"),
                            null,
                            null
                    );
                    logger.info("Успешно получены id, username, password_hash пользователя: {}", username);
                    return user;

                }
                else {
                    logger.info("Пользователь {} не найден", username);
                    return null;
                }
            }

        } catch (SQLException e) {
            logger.error("Ошибка при получении id, username, password у пользователя = {}", username, e);
            throw new DAOException("Ошибка при считывании данных из базы данных user", e);
        }

    }

    public void setRole(long id, String role) {
        logger.info("Изменение роли на {} у пользователя: {}", role, id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE users SET role = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при изменении роли у пользователя {}", id, e);
            throw new DAOException("Ошибка при изменении данных в базе данных user", e);
        }
    }

    public void setUsername(long id, String username) {
        logger.info("Изменение имени на {} у пользователя id = {}", username, id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE users SET username = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при изменении имени у пользователя {}", id, e);
            throw new DAOException("Ошибка при изменении данных в базе данных user", e);
        }
    }

    public void setPasswordHash(long id, String passwordHash) {
        logger.info("Изменение пароля у пользователя: id = {}", id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, passwordHash);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при изменении пароля у пользователя {}", id, e);
            throw new DAOException("Ошибка при изменении данных в базе данных user", e);
        }
    }

    public void setFactoryType(long id, String factoryType) {
        logger.info("Изменение фабрики на {} у пользователя: {}", factoryType, id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE users SET factory_type = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, factoryType);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при изменении фабрики у пользователя {}", id, e);
            throw new DAOException("Ошибка при изменении данных в базе данных user", e);
        }
    }
}