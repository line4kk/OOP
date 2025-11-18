package dao;

import model.User;
import org.junit.jupiter.api.*;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest {

    private UserDAO userDAO;
    private Connection conn;

    @BeforeAll
    void setup() {
        userDAO = new UserDAO();
        conn = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void cleanTable() throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users")) {
            stmt.executeUpdate();
        }
    }

    @Test
    void testInsertAndSelect() {
        User user = new User("alice", "hash123", "admin", "factoryA");
        userDAO.insert(user);

        User fetched = userDAO.select("alice");
        assertNotNull(fetched);
        assertEquals("alice", fetched.getUsername());
        assertEquals("admin", fetched.getRole());
        assertEquals("factoryA", fetched.getFactoryType());
        assertNull(fetched.getPasswordHash()); // select без пароля
    }

    @Test
    void testSelectCredentials() {
        User user = new User("bob", "hash456", "user", "factoryB");
        userDAO.insert(user);

        User fetched = userDAO.selectCredentials("bob");
        assertNotNull(fetched);
        assertEquals("bob", fetched.getUsername());
        assertEquals("hash456", fetched.getPasswordHash());
        assertNull(fetched.getRole());
        assertNull(fetched.getFactoryType());
    }

    @Test
    void testDeleteById() {
        User user = new User("charlie", "hash789", "user", "factoryC");
        userDAO.insert(user);

        User fetched = userDAO.select("charlie");
        assertNotNull(fetched);

        userDAO.deleteById(fetched.getId());

        User afterDelete = userDAO.select("charlie");
        assertNull(afterDelete);
    }

    @Test
    void testUpdateRole() {
        User user = new User("dave", "hash000", "user", "factoryD");
        userDAO.insert(user);

        User fetched = userDAO.select("dave");
        userDAO.setRole(fetched.getId(), "admin");

        User updated = userDAO.select("dave");
        assertEquals("admin", updated.getRole());
    }

    @Test
    void testUpdateUsername() {
        User user = new User("eve", "hash111", "user", "factoryE");
        userDAO.insert(user);

        User fetched = userDAO.select("eve");
        userDAO.setUsername(fetched.getId(), "eve_new");

        assertNull(userDAO.select("eve"));
        assertNotNull(userDAO.select("eve_new"));
    }

    @Test
    void testUpdatePasswordAndFactory() {
        User user = new User("frank", "oldHash", "user", "factoryF");
        userDAO.insert(user);

        User fetched = userDAO.selectCredentials("frank");
        userDAO.setPasswordHash(fetched.getId(), "newHash");
        userDAO.setFactoryType(fetched.getId(), "factoryNew");

        User updatedCredentials = userDAO.selectCredentials("frank");
        User updatedUser = userDAO.select("frank");

        assertEquals("newHash", updatedCredentials.getPasswordHash());
        assertEquals("factoryNew", updatedUser.getFactoryType());
    }
}
