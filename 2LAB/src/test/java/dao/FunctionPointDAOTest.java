package dao;

import model.Function;
import model.FunctionPoint;
import org.junit.jupiter.api.*;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionPointDAOTest {

    private FunctionPointDAO pointDAO;
    private FunctionDAO functionDAO;
    private UserDAO userDAO;
    private Connection conn;

    @BeforeAll
    void setup() {
        pointDAO = new FunctionPointDAO();
        functionDAO = new FunctionDAO();
        userDAO = new UserDAO();
        conn = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void cleanTables() throws SQLException {
        conn.prepareStatement("DELETE FROM function_points").executeUpdate();
        conn.prepareStatement("DELETE FROM functions").executeUpdate();
        conn.prepareStatement("DELETE FROM users").executeUpdate();
    }

    private long createFunction() {
        userDAO.insert(new model.User("u", "p", "r", "f"));
        long userId = userDAO.select("u").getId();

        functionDAO.insert(new Function(userId, "f", "tab", "source"));
        return functionDAO.selectByUserId(userId).get(0).getId();
    }

    @Test
    void testInsertAndSelectById() {
        long functionId = createFunction();

        FunctionPoint p = new FunctionPoint(functionId, 1.0, 2.0);
        pointDAO.insert(p);

        List<FunctionPoint> list = pointDAO.selectByFunctionId(functionId);
        assertEquals(1, list.size());

        FunctionPoint fetched = list.get(0);

        FunctionPoint byId = pointDAO.selectById(fetched.getId());
        assertNotNull(byId);
        assertEquals(1.0, byId.getXValue());
        assertEquals(2.0, byId.getYValue());
    }

    @Test
    void testSelectByFunctionIdMultiple() {
        long functionId = createFunction();

        pointDAO.insert(new FunctionPoint(functionId, 1.0, 1.0));
        pointDAO.insert(new FunctionPoint(functionId, 2.0, 4.0));
        pointDAO.insert(new FunctionPoint(functionId, 3.0, 9.0));

        List<FunctionPoint> list = pointDAO.selectByFunctionId(functionId);
        assertEquals(3, list.size());

        // Ordered by x_value
        assertEquals(1.0, list.get(0).getXValue());
        assertEquals(2.0, list.get(1).getXValue());
        assertEquals(3.0, list.get(2).getXValue());
    }

    @Test
    void testUpdateYValue() {
        long functionId = createFunction();

        pointDAO.insert(new FunctionPoint(functionId, 5.0, 10.0));

        pointDAO.updateYValue(functionId, 5.0, 20.0);

        FunctionPoint p = pointDAO.selectByFunctionId(functionId).get(0);
        assertEquals(20.0, p.getYValue());
    }

    @Test
    void testDeleteById() {
        long functionId = createFunction();

        pointDAO.insert(new FunctionPoint(functionId, 1.0, 1.0));
        FunctionPoint p = pointDAO.selectByFunctionId(functionId).get(0);

        pointDAO.deleteById(p.getId());
        assertNull(pointDAO.selectById(p.getId()));
    }

    @Test
    void testDeleteByFunctionId() {
        long functionId = createFunction();

        pointDAO.insert(new FunctionPoint(functionId, 1.0, 1.0));
        pointDAO.insert(new FunctionPoint(functionId, 2.0, 2.0));
        pointDAO.insert(new FunctionPoint(functionId, 3.0, 3.0));

        pointDAO.deleteByFunctionId(functionId);

        List<FunctionPoint> list = pointDAO.selectByFunctionId(functionId);
        assertTrue(list.isEmpty());
    }

    @Test
    void testCascadeDeleteFunction() throws SQLException {
        long functionId = createFunction();

        pointDAO.insert(new FunctionPoint(functionId, 1.0, 2.0));

        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM functions WHERE id = ?")) {
            pstmt.setLong(1, functionId);
            pstmt.executeUpdate();
        }

        List<FunctionPoint> list = pointDAO.selectByFunctionId(functionId);
        assertTrue(list.isEmpty());
    }
}
