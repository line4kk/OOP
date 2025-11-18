package dao;

import dao.criteria.FunctionPointSearchCriteria;
import dao.criteria.SortDirection;
import exceptions.DAOException;
import model.FunctionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FunctionPointDAO implements SearchableDAO<FunctionPoint, FunctionPointSearchCriteria> {
    private static final Logger logger = LoggerFactory.getLogger(FunctionPointDAO.class);

    @Override
    public void insert(FunctionPoint point) {
        logger.info("Вставка строки в таблицу function_points: {}", point);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO function_points (function_id, x_value, y_value) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, point.getFunctionId());
            pstmt.setDouble(2, point.getXValue());
            pstmt.setDouble(3, point.getYValue());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при вставке данных в таблицу function_points", e);
            throw new DAOException("Ошибка при вставке данных в базу данных function_points", e);
        }
    }

    @Override
    public void deleteById(long id) {
        logger.info("Удаление строки из таблицы function_points: id = {}", id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM function_points WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при удалении строки из таблицы function_points: id = {}", id, e);
            throw new DAOException("Ошибка при удалении данных из базы данных function_points", e);
        }
    }

    public FunctionPoint selectById(long id) {
        logger.info("Получение строки FunctionPoint по id={}", id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM function_points WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    FunctionPoint point = new FunctionPoint(
                            resultSet.getLong("id"),
                            resultSet.getLong("function_id"),
                            resultSet.getDouble("x_value"),
                            resultSet.getDouble("y_value")
                    );
                    logger.info("Успешно получена строка: {}", point);
                    return point;
                } else {
                    logger.info("FunctionPoint с id={} не найден", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении FunctionPoint с id={}", id, e);
            throw new DAOException("Ошибка при считывании данных из базы данных function_points", e);
        }
    }

    public List<FunctionPoint> selectByFunctionId(long functionId) {
        logger.info("Получение списка FunctionPoint по function_id={}", functionId);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT id, x_value, y_value FROM function_points WHERE function_id = ? ORDER BY x_value";
        List<FunctionPoint> points = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, functionId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    FunctionPoint point = new FunctionPoint(
                            resultSet.getLong("id"),
                            functionId,
                            resultSet.getDouble("x_value"),
                            resultSet.getDouble("y_value")
                    );
                    points.add(point);
                }
                logger.info("Успешно получены {} точек для function_id={}", points.size(), functionId);
                return points;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении FunctionPoint для function_id={}", functionId, e);
            throw new DAOException("Ошибка при считывании данных из базы данных function_points", e);
        }
    }

    public void updateYValue(long functionId, double xValue, double yValue) {
        logger.info("Изменение y_value на {} для function_id={} и x_value={}", yValue, functionId, xValue);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE function_points SET y_value = ? WHERE function_id = ? AND x_value = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, yValue);
            pstmt.setLong(2, functionId);
            pstmt.setDouble(3, xValue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении y_value для function_id={} и x_value={}", functionId, xValue, e);
            throw new DAOException("Ошибка при изменении данных в базе данных function_points", e);
        }
    }

    public void deleteByFunctionId(long functionId) {
        logger.info("Удаление всех точек для function_id={}", functionId);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM function_points WHERE function_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, functionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при удалении точек для function_id={}", functionId, e);
            throw new DAOException("Ошибка при удалении данных из базы данных function_points", e);
        }
    }

    @Override
    public List<FunctionPoint> search(FunctionPointSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new FunctionPointSearchCriteria();
        }

        logger.info("Поиск точек функции по критериям: {}", criteria);

        StringBuilder sql = new StringBuilder(
                "SELECT id, function_id, x_value, y_value FROM function_points WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (criteria.getFunctionId() != null) {
            sql.append(" AND function_id = ?");
            params.add(criteria.getFunctionId());
        }
        if (criteria.getxFrom() != null) {
            sql.append(" AND x_value >= ?");
            params.add(criteria.getxFrom());
        }
        if (criteria.getxTo() != null) {
            sql.append(" AND x_value <= ?");
            params.add(criteria.getxTo());
        }
        if (criteria.getyFrom() != null) {
            sql.append(" AND y_value >= ?");
            params.add(criteria.getyFrom());
        }
        if (criteria.getyTo() != null) {
            sql.append(" AND y_value <= ?");
            params.add(criteria.getyTo());
        }

        // Сортировка
        String orderColumn = switch (criteria.getSortBy()) {
            case FUNCTION_ID -> "function_id";
            case X_VALUE     -> "x_value";
            case Y_VALUE     -> "y_value";
            default          -> "id";
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
                List<FunctionPoint> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new FunctionPoint(
                            rs.getLong("id"),
                            rs.getLong("function_id"),
                            rs.getDouble("x_value"),
                            rs.getDouble("y_value")
                    ));
                }
                logger.info("Найдено точек: {}", result.size());
                return result;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек функции", e);
            throw new DAOException("Ошибка поиска точек функции", e);
        }
    }
}
