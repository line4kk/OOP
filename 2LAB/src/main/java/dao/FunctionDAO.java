package dao;

import dao.criteria.FunctionSearchCriteria;
import dao.criteria.SortDirection;
import exceptions.DAOException;
import model.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FunctionDAO implements SearchableDAO<Function, FunctionSearchCriteria> {

    private static final Logger logger = LoggerFactory.getLogger(FunctionDAO.class);

    @Override
    public void insert(Function function) {
        logger.info("Вставка строки в таблицу functions: {}", function);

        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO functions (user_id, name, type, source) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, function.getUserId());
            pstmt.setString(2, function.getName());
            pstmt.setString(3, function.getType());
            pstmt.setString(4, function.getSource());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Ошибка при вставке данных в таблицу functions: {}", function, e);
            throw new DAOException("Ошибка при вставке данных в базу данных functions", e);
        }
    }

    @Override
    public void deleteById(long id) {
        logger.info("Удаление функции id={}", id);

        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM functions WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Ошибка при удалении функции id={}", id, e);
            throw new DAOException("Ошибка при удалении данных из базы данных functions", e);
        }
    }

    public List<Function> selectByUserId(long userId) {
        logger.info("Получение всех функций пользователя userId={}", userId);

        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM functions WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Function> list = new ArrayList<>();

                while (rs.next()) {
                    list.add(new Function(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getString("source")
                    ));
                }

                logger.info("Найдено {} функций у пользователя {}", list.size(), userId);
                return list;
            }

        } catch (SQLException e) {
            logger.error("Ошибка при получении функций по userId={}", userId, e);
            throw new DAOException("Ошибка при считывании данных из базы данных functions", e);
        }
    }

    public Function selectById(long id) {
        logger.info("Получение функции id={}", id);

        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM functions WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Function function = new Function(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getString("source")
                    );

                    logger.info("Функция успешно получена: {}", function);
                    return function;
                } else {
                    logger.info("Функция id={} не найдена", id);
                    return null;
                }
            }

        } catch (SQLException e) {
            logger.error("Ошибка при получении функции id={}", id, e);
            throw new DAOException("Ошибка при считывании данных из базы данных functions", e);
        }
    }

    public void setName(long id, String name) {
        logger.info("Изменение имени функции id={} на '{}'", id, name);

        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE functions SET name = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Ошибка при изменении имени функции id={}", id, e);
            throw new DAOException("Ошибка при изменении данных в базе данных functions", e);
        }
    }

    public void deleteByUserId(long userId) {
        logger.info("Удаление всех функций пользователя userId={}", userId);

        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM functions WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Ошибка при удалении функций пользователя userId={}", userId, e);
            throw new DAOException("Ошибка при удалении данных из базы данных functions", e);
        }
    }

    public List<Function> search(FunctionSearchCriteria criteria) {
        logger.info("Поиск функций по критериям: {}", criteria);

        StringBuilder sql = new StringBuilder("SELECT id, user_id, name, type, source FROM functions WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (criteria.getUserId() != null) {
            sql.append(" AND user_id = ?");
            params.add(criteria.getUserId());
        }
        if (criteria.getNameContains() != null && !criteria.getNameContains().isBlank()) {
            sql.append(" AND name ILIKE ?");
            params.add("%" + criteria.getNameContains() + "%");
        }
        if (criteria.getType() != null && !criteria.getType().isBlank()) {
            sql.append(" AND type = ?");
            params.add(criteria.getType());
        }

        String orderColumn = switch (criteria.getSortBy()) {
            case NAME -> "name";
            case TYPE -> "type";
            case USER_ID -> "user_id";
            default -> "id";
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
                List<Function> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new Function(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getString("source")
                    ));
                }
                logger.info("Найдено {} функций", result.size());
                return result;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций", e);
            throw new DAOException("Ошибка поиска функций", e);
        }
    }
}
