package dao;

import dao.criteria.UserSearchCriteria;
import dao.criteria.SortDirection;
import exceptions.DAOException;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UserDAO implements SearchableDAO<User, UserSearchCriteria> {
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

    @Override
    public List<User> search(UserSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new UserSearchCriteria();
        }

        logger.info("Поиск пользователей по критериям: {}", criteria);

        StringBuilder sql = new StringBuilder(
                "SELECT id, username, password_hash, role, factory_type FROM users WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (criteria.getUsernameContains() != null && !criteria.getUsernameContains().isBlank()) {
            sql.append(" AND username ILIKE ?");
            params.add("%" + criteria.getUsernameContains() + "%");
        }
        if (criteria.getRole() != null && !criteria.getRole().isBlank()) {
            sql.append(" AND role = ?");
            params.add(criteria.getRole());
        }
        if (criteria.getFactoryType() != null && !criteria.getFactoryType().isBlank()) {
            sql.append(" AND factory_type = ?");
            params.add(criteria.getFactoryType());
        }

        // Сортировка
        String orderColumn = switch (criteria.getSortBy()) {
            case USERNAME      -> "username";
            case ROLE          -> "role";
            case FACTORY_TYPE  -> "factory_type";
            case ID            -> "id";
        };
        sql.append(" ORDER BY ").append(orderColumn)
                .append(" ").append(criteria.getDirection() == SortDirection.DESC ? "DESC" : "ASC");

        logger.info("SQL: {}", sql);
        logger.info("Параметры: {}", params);

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<User> result = new ArrayList<>();
                while (rs.next()) {
                    User user = new User(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("factory_type")
                    );
                    result.add(user);
                }
                logger.info("Найдено пользователей: {}", result.size());
                return result;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователей", e);
            throw new DAOException("Ошибка поиска пользователей", e);
        }
    }
}