package dao;

import model.Function;
import org.junit.jupiter.api.*;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionDAOTest {

    private FunctionDAO functionDAO;
    private Connection conn;
    private UserDAO userDAO;

    @BeforeAll
    void setup() {
        functionDAO = new FunctionDAO();
        userDAO = new UserDAO();
        conn = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void cleanTables() throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM functions")) {
            pstmt.executeUpdate();
        }
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users")) {
            pstmt.executeUpdate();
        }
    }

    private long createTestUser() {
        userDAO.insert(new model.User("testuser", "pass", "role", "factory"));
        return userDAO.select("testuser").getId();
    }

    @Test
    void testInsertAndSelectById() {
        long userId = createTestUser();

        Function f = new Function(userId, "f1", "tab", "source");
        functionDAO.insert(f);

        List<Function> list = functionDAO.selectByUserId(userId);
        assertEquals(1, list.size());

        Function fetched = list.get(0);
        assertEquals("f1", fetched.getName());
        assertEquals("tab", fetched.getType());
        assertEquals("source", fetched.getSource());

        Function byId = functionDAO.selectById(fetched.getId());
        assertNotNull(byId);
        assertEquals(fetched.getId(), byId.getId());
    }

    @Test
    void testSelectByUserIdMultiple() {
        long userId = createTestUser();

        functionDAO.insert(new Function(userId, "f1", "t1", "src1"));
        functionDAO.insert(new Function(userId, "f2", "t2", "src2"));
        functionDAO.insert(new Function(userId, "f3", "t3", "src3"));

        List<Function> list = functionDAO.selectByUserId(userId);
        assertEquals(3, list.size());
    }

    @Test
    void testSetName() {
        long userId = createTestUser();

        functionDAO.insert(new Function(userId, "oldName", "tab", "src"));
        Function f = functionDAO.selectByUserId(userId).get(0);

        functionDAO.setName(f.getId(), "newName");

        Function updated = functionDAO.selectById(f.getId());
        assertEquals("newName", updated.getName());
    }

    @Test
    void testDeleteById() {
        long userId = createTestUser();

        functionDAO.insert(new Function(userId, "testFunc", "tab", "source"));
        Function f = functionDAO.selectByUserId(userId).get(0);

        functionDAO.deleteById(f.getId());

        assertNull(functionDAO.selectById(f.getId()));
    }

    @Test
    void testDeleteByUserId() {
        long userId = createTestUser();

        functionDAO.insert(new Function(userId, "a", "t", "s"));
        functionDAO.insert(new Function(userId, "b", "t", "s"));
        functionDAO.insert(new Function(userId, "c", "t", "s"));

        functionDAO.deleteByUserId(userId);

        List<Function> list = functionDAO.selectByUserId(userId);
        assertTrue(list.isEmpty());
    }

    @Test
    void testCascadeDeleteUser() throws SQLException {
        long userId = createTestUser();

        functionDAO.insert(new Function(userId, "f", "t", "s"));

        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            pstmt.setLong(1, userId);
            pstmt.executeUpdate();
        }

        List<Function> list = functionDAO.selectByUserId(userId);
        assertEquals(0, list.size());
    }
}
