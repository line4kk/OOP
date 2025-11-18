package dao;

import dao.criteria.CompositeFunctionElementSearchCriteria;
import dao.criteria.SortDirection;
import exceptions.DAOException;
import model.CompositeFunctionElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompositeFunctionElementDAO implements SearchableDAO<CompositeFunctionElement, CompositeFunctionElementSearchCriteria> {
    private static final Logger logger = LoggerFactory.getLogger(CompositeFunctionElementDAO.class);

    @Override
    public void insert(CompositeFunctionElement compositeFunctionElement) {
        logger.info("Вставка строки в таблицу composite_function_elements: {}", compositeFunctionElement);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO composite_function_elements (composite_id, function_order, function_id) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, compositeFunctionElement.getCompositeId());
            pstmt.setInt(2, compositeFunctionElement.getFunctionOrder());
            pstmt.setLong(3, compositeFunctionElement.getFunctionId());
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            logger.error("Ошибка при вставке данных в таблицу composite_functions_elements", e);
            throw new DAOException("Ошибка при вставке данных в базу данных composite_function_elements", e);
        }
    }

    @Override
    public void deleteById(long id) {
        logger.info("Удаление строки из таблицы composite_functions_elements по id = {}", id);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "DELETE FROM composite_function_elements WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            logger.error("Ошибка при удалении данных из таблицы composite_functions_elements: id={}", id, e);
            throw new DAOException("Ошибка при удалении данных из базы данных composite_functions_elements", e);
        }
    }

    public List<CompositeFunctionElement> selectAll(long compositeFunctionId) {
        logger.info("Получение всех элементов композиции {}", compositeFunctionId);
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM composite_function_elements WHERE composite_id = ?";
        List<CompositeFunctionElement> elements = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, compositeFunctionId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    CompositeFunctionElement element = new CompositeFunctionElement(
                            resultSet.getLong("id"),
                            resultSet.getLong("composite_id"),
                            resultSet.getInt("function_order"),
                            resultSet.getLong("function_id")
                    );
                    elements.add(element);
                }

                logger.info("Успешно получены {} элементов для композиции {}", elements.size(), compositeFunctionId);
                return elements;
            }
        }
        catch (SQLException e) {
            logger.error("Ошибка при получении элементов композиции {}", compositeFunctionId, e);
            throw new DAOException("Ошибка при считывании данных из базы данных composite_function_elements", e);
        }
    }

    @Override
    public List<CompositeFunctionElement> search(CompositeFunctionElementSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new CompositeFunctionElementSearchCriteria();
        }

        logger.info("Поиск элементов композиции по критериям: {}", criteria);

        StringBuilder sql = new StringBuilder(
                "SELECT id, composite_id, function_order, function_id " +
                        "FROM composite_function_elements WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (criteria.getCompositeId() != null) {
            sql.append(" AND composite_id = ?");
            params.add(criteria.getCompositeId());
        }
        if (criteria.getOrderFrom() != null) {
            sql.append(" AND function_order >= ?");
            params.add(criteria.getOrderFrom());
        }
        if (criteria.getOrderTo() != null) {
            sql.append(" AND function_order <= ?");
            params.add(criteria.getOrderTo());
        }
        if (criteria.getFunctionId() != null) {
            sql.append(" AND function_id = ?");
            params.add(criteria.getFunctionId());
        }

        // Сортировка (по умолчанию — по порядку в композиции)
        String orderColumn = switch (criteria.getSortBy()) {
            case COMPOSITE_ID    -> "composite_id";
            case FUNCTION_ID     -> "function_id";
            case FUNCTION_ORDER  -> "function_order";
            default              -> "id";
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
                List<CompositeFunctionElement> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new CompositeFunctionElement(
                            rs.getLong("id"),
                            rs.getLong("composite_id"),
                            rs.getInt("function_order"),
                            rs.getLong("function_id")
                    ));
                }
                logger.info("Найдено элементов композиции: {}", result.size());
                return result;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске элементов композиции", e);
            throw new DAOException("Ошибка поиска элементов композиции", e);
        }
    }


}
