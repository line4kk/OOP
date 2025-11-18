package dao;

import model.Function;
import model.FunctionPoint;
import model.OperationResultPoint;
import model.User;
import org.junit.jupiter.api.*;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OperationResultPointDAOTest {

    private OperationResultPointDAO dao;
    private FunctionDAO functionDAO;
    private FunctionPointDAO functionPointDAO;
    private Connection conn;

    private long point1Id;
    private long point2Id;
    private long point3Id;

    @BeforeAll
    void globalSetup() {
        dao = new OperationResultPointDAO();
        functionDAO = new FunctionDAO();
        functionPointDAO = new FunctionPointDAO();
        conn = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM operations_result_points; " +
                        "DELETE FROM function_points; " +
                        "DELETE FROM functions; " +
                        "DELETE FROM users;")) {
            stmt.executeUpdate();
        }

        UserDAO userDAO = new UserDAO();
        userDAO.insert(new User("testuser", "hash", "user", "test"));

        User user = userDAO.select("testuser");
        Function func = new Function(user.getId(), "testFunc", "POLYNOMIAL", "source");
        functionDAO.insert(func);
        Function insertedFunc = functionDAO.selectByUserId(user.getId()).get(0);

        FunctionPoint p1 = new FunctionPoint(insertedFunc.getId(), 1.0, 10.0);
        FunctionPoint p2 = new FunctionPoint(insertedFunc.getId(), 2.0, 20.0);
        FunctionPoint p3 = new FunctionPoint(insertedFunc.getId(), 3.0, 30.0);

        functionPointDAO.insert(p1);
        functionPointDAO.insert(p2);
        functionPointDAO.insert(p3);

        point1Id = functionPointDAO.selectByFunctionId(insertedFunc.getId()).get(0).getId();
        point2Id = functionPointDAO.selectByFunctionId(insertedFunc.getId()).get(1).getId();
        point3Id = functionPointDAO.selectByFunctionId(insertedFunc.getId()).get(2).getId();
    }

    @Test
    void testInsertAndSelectCommutative() {
        OperationResultPoint point = new OperationResultPoint(
                point1Id, point2Id, "ADD", 30.0);

        dao.insert(point);

        Double result = dao.getResultYOfCommutativeOperation("ADD", point1Id, point2Id);
        assertEquals(30.0, result, 0.0001);

        // Коммутативность: порядок точек не важен
        Double resultReversed = dao.getResultYOfCommutativeOperation("ADD", point2Id, point1Id);
        assertEquals(30.0, resultReversed, 0.0001);
    }

    @Test
    void testInsertAndSelectNonCommutative() {
        OperationResultPoint point = new OperationResultPoint(
                point1Id, point2Id, "SUBTRACT", 15.0);

        dao.insert(point);

        Double result = dao.getResultYOfNonCommutativeOperation("SUBTRACT", point1Id, point2Id);
        assertEquals(15.0, result, 0.0001);

        // Для некоммутативной операции обратный порядок — ничего не найдёт
        Double resultReversed = dao.getResultYOfNonCommutativeOperation("SUBTRACT", point2Id, point1Id);
        assertNull(resultReversed);
    }

    @Test
    void testDeleteById() {
        OperationResultPoint point = new OperationResultPoint(point1Id, point2Id, "MULTIPLY", 200.0);
        dao.insert(point);

        assertNotNull(dao.getResultYOfCommutativeOperation("MULTIPLY", point1Id, point2Id));

        // Находим id вставленной строки (прямой запрос, т.к. в DAO нет selectById)
        long insertedId = getLastInsertedId();

        dao.deleteById(insertedId);

        Double afterDelete = dao.getResultYOfCommutativeOperation("MULTIPLY", point1Id, point2Id);
        assertNull(afterDelete);
    }

    @Test
    void testDeleteByOperationAndPoints_Commutative() {
        dao.insert(new OperationResultPoint(point1Id, point2Id, "ADD", 100.0));
        dao.insert(new OperationResultPoint(point2Id, point3Id, "ADD", 200.0));

        assertNotNull(dao.getResultYOfCommutativeOperation("ADD", point1Id, point2Id));

        dao.deleteByOperationAndPoints("ADD", point1Id, point2Id);

        assertNull(dao.getResultYOfCommutativeOperation("ADD", point1Id, point2Id));
        // Другая пара осталась нетронутой
        assertNotNull(dao.getResultYOfCommutativeOperation("ADD", point2Id, point3Id));
    }

    @Test
    void testDeleteByOperationAndPointsExact_NonCommutative() {
        dao.insert(new OperationResultPoint(point1Id, point2Id, "DIVIDE", 5.0));
        dao.insert(new OperationResultPoint(point2Id, point1Id, "DIVIDE", 0.2));

        assertEquals(5.0, dao.getResultYOfNonCommutativeOperation("DIVIDE", point1Id, point2Id));

        dao.deleteByOperationAndPointsExact("DIVIDE", point1Id, point2Id);

        assertNull(dao.getResultYOfNonCommutativeOperation("DIVIDE", point1Id, point2Id));
        // Обратная запись осталась
        assertEquals(0.2, dao.getResultYOfNonCommutativeOperation("DIVIDE", point2Id, point1Id));
    }

    @Test
    void testTriggerDeletesOperationResults_WhenPointUpdated() {
        // Вставляем результат операции
        dao.insert(new OperationResultPoint(point1Id, point2Id, "ADD", 999.0));
        assertNotNull(dao.getResultYOfCommutativeOperation("ADD", point1Id, point2Id));

        // Обновляем y_value одной из точек → должен сработать триггер
        functionPointDAO.updateYValue(
                functionPointDAO.selectById(point1Id).getFunctionId(),
                1.0,
                9999.0
        );

        // Результат операции должен исчезнуть
        Double resultAfterUpdate = dao.getResultYOfCommutativeOperation("ADD", point1Id, point2Id);
        assertNull(resultAfterUpdate);
    }


    // Вспомогательный метод — получаем последний вставленный id в operations_result_points
    private long getLastInsertedId() {
        String sql = "SELECT id FROM operations_result_points ORDER BY id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("id");
            }
            throw new IllegalStateException("Нет записей в operations_result_points");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}