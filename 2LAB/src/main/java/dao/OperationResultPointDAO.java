package dao;

import dao.criteria.OperationResultPointSearchCriteria;
import dao.criteria.SortDirection;
import exceptions.DAOException;
import model.OperationResultPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OperationResultPointDAO implements SearchableDAO<OperationResultPoint, OperationResultPointSearchCriteria> {
    private static final Logger logger = LoggerFactory.getLogger(OperationResultPointDAO.class);

    @Override
    public void insert(OperationResultPoint point) {
        logger.info("Вставка строки в таблицу operations_result_points: {}", point);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO operations_result_points (point1_id, point2_id, operation, result_y) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, point.getPoint1Id());
            pstmt.setLong(2, point.getPoint2Id());
            pstmt.setString(3, point.getOperation());
            pstmt.setDouble(4, point.getResultY());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при вставке данных в таблицу operations_result_points", e);
            throw new DAOException("Ошибка при вставке данных в базу данных operations_result_points", e);
        }
    }

    @Override
    public void deleteById(long id) {
        logger.info("Удаление строки из таблицы operations_result_points по id = {}", id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM operations_result_points WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при удалении строки из таблицы operations_result_points: id = {}", id, e);
            throw new DAOException("Ошибка при удалении данных из базы данных operations_result_points", e);
        }
    }

    public Double getResultYOfCommutativeOperation(String operation, long point1Id, long point2Id) {
        logger.info("Получение result_y коммутативной операции {} и точек ({}, {})", operation, point1Id, point2Id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT result_y FROM operations_result_points " +
                "WHERE operation = ? AND ((point1_id = ? AND point2_id = ?) OR (point2_id = ? AND point1_id = ?))";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, operation);
            pstmt.setLong(2, point1Id);
            pstmt.setLong(3, point2Id);
            pstmt.setLong(4, point1Id);
            pstmt.setLong(5, point2Id);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    double resultY = resultSet.getDouble("result_y");
                    logger.info("Успешно получен result_y = {} коммутативной операции {} и точек ({}, {})", resultY, operation, point1Id, point2Id);
                    return resultY;
                } else {
                    logger.info("Результат для коммутативной операции {} и точек ({}, {}) не найден", operation, point1Id, point2Id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении result_y для коммутативной операции {} и точек ({}, {})", operation, point1Id, point2Id, e);
            throw new DAOException("Ошибка при считывании данных из базы данных operations_result_points", e);
        }
    }

    public Double getResultYOfNonCommutativeOperation(String operation, long point1Id, long point2Id) {
        logger.info("Получение result_y для некоммутативной операции {} и точек ({} , {})", operation, point1Id, point2Id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT result_y FROM operations_result_points WHERE operation = ? AND point1_id = ? AND point2_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, operation);
            pstmt.setLong(2, point1Id);
            pstmt.setLong(3, point2Id);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    double resultY = resultSet.getDouble("result_y");
                    logger.info("Успешно получен result_y={} для некоммутативной операции {} и точек ({}, {})", resultY, operation, point1Id, point2Id);
                    return resultY;
                } else {
                    logger.info("Результат для некоммутативной операции {} и точек ({}, {}) не найден", operation, point1Id, point2Id);
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении result_y для некоммутативной операции {} и точек ({}, {})", operation, point1Id, point2Id, e);
            throw new DAOException("Ошибка при считывании данных из базы данных operations_result_points", e);
        }
    }

    public void deleteByOperationAndPoints(String operation, long point1Id, long point2Id) {
        logger.info("Удаление результата операции {} для точек ({}, {})", operation, point1Id, point2Id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM operations_result_points " +
                "WHERE operation = ? AND ((point1_id = ? AND point2_id = ?) OR (point2_id = ? AND point1_id = ?))";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, operation);
            pstmt.setLong(2, point1Id);
            pstmt.setLong(3, point2Id);
            pstmt.setLong(4, point1Id);
            pstmt.setLong(5, point2Id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при удалении результата операции {} для точек ({}, {})", operation, point1Id, point2Id, e);
            throw new DAOException("Ошибка при удалении данных из базы данных operations_result_points", e);
        }
    }

    public void deleteByOperationAndPointsExact(String operation, long point1Id, long point2Id) {
        logger.info("Удаление результата операции {} для точек ({}, {}) строго по порядку", operation, point1Id, point2Id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM operations_result_points WHERE operation = ? AND point1_id = ? AND point2_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, operation);
            pstmt.setLong(2, point1Id);
            pstmt.setLong(3, point2Id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при удалении результата операции {} для точек ({}, {})", operation, point1Id, point2Id, e);
            throw new DAOException("Ошибка при удалении данных из базы данных operations_result_points", e);
        }
    }

    @Override
    public List<OperationResultPoint> search(OperationResultPointSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new OperationResultPointSearchCriteria();
        }

        logger.info("Поиск результатов операций по критериям: {}", criteria);

        StringBuilder sql = new StringBuilder(
                "SELECT id, point1_id, point2_id, operation, result_y " +
                        "FROM operations_result_points WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (criteria.getPoint1Id() != null) {
            sql.append(" AND point1_id = ?");
            params.add(criteria.getPoint1Id());
        }
        if (criteria.getPoint2Id() != null) {
            sql.append(" AND point2_id = ?");
            params.add(criteria.getPoint2Id());
        }
        if (criteria.getOperation() != null && !criteria.getOperation().isBlank()) {
            sql.append(" AND operation = ?");
            params.add(criteria.getOperation());
        }
        if (criteria.getResultYFrom() != null) {
            sql.append(" AND result_y >= ?");
            params.add(criteria.getResultYFrom());
        }
        if (criteria.getResultYTo() != null) {
            sql.append(" AND result_y <= ?");
            params.add(criteria.getResultYTo());
        }

        // Сортировка
        String orderColumn = switch (criteria.getSortBy()) {
            case POINT1_ID    -> "point1_id";
            case POINT2_ID    -> "point2_id";
            case OPERATION    -> "operation";
            case RESULT_Y     -> "result_y";
            default           -> "id";
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
                List<OperationResultPoint> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new OperationResultPoint(
                            rs.getLong("id"),
                            rs.getLong("point1_id"),
                            rs.getLong("point2_id"),
                            rs.getString("operation"),
                            rs.getDouble("result_y")
                    ));
                }
                logger.info("Найдено результатов операций: {}", result.size());
                return result;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске результатов операций", e);
            throw new DAOException("Ошибка поиска результатов операций", e);
        }
    }
}
