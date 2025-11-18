package dao;

import exceptions.DAOException;
import model.CompositeFunctionElement;
import model.Function;
import model.User;
import org.junit.jupiter.api.*;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompositeFunctionElementDAOTest {

    private CompositeFunctionElementDAO dao;
    private FunctionDAO functionDAO;
    private UserDAO userDAO;
    private Connection conn;

    private long userId;
    private long compositeFunctionId;
    private long componentFunc1Id;
    private long componentFunc2Id;

    @BeforeAll
    void globalSetup() {
        dao = new CompositeFunctionElementDAO();
        functionDAO = new FunctionDAO();
        userDAO = new UserDAO();
        conn = DatabaseConnection.getConnection();
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Полная очистка всех таблиц (порядок важен из-за FK)
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM composite_function_elements; " +
                        "DELETE FROM function_points; " +
                        "DELETE FROM operations_result_points; " +
                        "DELETE FROM functions; " +
                        "DELETE FROM users;"
        )) {
            stmt.executeUpdate();
        }

        userDAO.insert(new User("compUser", "hash123", "user", "test"));
        User user = userDAO.select("compUser");
        userId = user.getId();

        // 1. Композитная функция
        Function composite = new Function(userId, "MyComposite", "COMPOSITE", "source");
        functionDAO.insert(composite);

        // 2 и 3 — обычные функции-компоненты
        Function f1 = new Function(userId, "Sin", "TRIGONOMETRIC", "sin(x)");
        Function f2 = new Function(userId, "Square", "POLYNOMIAL", "x^2");

        functionDAO.insert(f1);
        functionDAO.insert(f2);

        // Получаем их id
        List<Function> allFuncs = functionDAO.selectByUserId(userId);
        compositeFunctionId = allFuncs.stream()
                .filter(f -> "MyComposite".equals(f.getName()))
                .findFirst().get().getId();

        componentFunc1Id = allFuncs.stream()
                .filter(f -> "Sin".equals(f.getName()))
                .findFirst().get().getId();

        componentFunc2Id = allFuncs.stream()
                .filter(f -> "Square".equals(f.getName()))
                .findFirst().get().getId();
    }

    @Test
    void testInsertAndSelectAll() {
        // Вставляем два элемента композиции
        CompositeFunctionElement elem1 = new CompositeFunctionElement(
                compositeFunctionId, 1, componentFunc1Id);
        CompositeFunctionElement elem2 = new CompositeFunctionElement(
                compositeFunctionId, 2, componentFunc2Id);

        dao.insert(elem1);
        dao.insert(elem2);

        List<CompositeFunctionElement> elements = dao.selectAll(compositeFunctionId);

        assertEquals(2, elements.size());

        // Проверяем порядок
        assertEquals(1, elements.get(0).getFunctionOrder());
        assertEquals(componentFunc1Id, elements.get(0).getFunctionId());

        assertEquals(2, elements.get(1).getFunctionOrder());
        assertEquals(componentFunc2Id, elements.get(1).getFunctionId());
    }

    @Test
    void testSelectAll_EmptyResult() {
        List<CompositeFunctionElement> elements = dao.selectAll(compositeFunctionId);
        assertTrue(elements.isEmpty());
    }

    @Test
    void testDeleteById() {
        CompositeFunctionElement elem = new CompositeFunctionElement(
                compositeFunctionId, 1, componentFunc1Id);
        dao.insert(elem);

        List<CompositeFunctionElement> before = dao.selectAll(compositeFunctionId);
        assertEquals(1, before.size());
        long idToDelete = before.get(0).getId();

        dao.deleteById(idToDelete);

        List<CompositeFunctionElement> after = dao.selectAll(compositeFunctionId);
        assertTrue(after.isEmpty());
    }

    @Test
    void testCascadeDelete_WhenCompositeFunctionDeleted() {
        // Вставляем элементы
        dao.insert(new CompositeFunctionElement(compositeFunctionId, 1, componentFunc1Id));
        dao.insert(new CompositeFunctionElement(compositeFunctionId, 2, componentFunc2Id));

        assertEquals(2, dao.selectAll(compositeFunctionId).size());

        // Удаляем композитную функцию (ON DELETE CASCADE)
        functionDAO.deleteById(compositeFunctionId);

        // Все элементы должны исчезнуть
        List<CompositeFunctionElement> remaining = dao.selectAll(compositeFunctionId);
        assertTrue(remaining.isEmpty());
    }

    @Test
    void testRestrictDelete_WhenComponentFunctionDeleted() {
        dao.insert(new CompositeFunctionElement(compositeFunctionId, 1, componentFunc1Id));

        // Попытка удалить функцию-компонент должна упасть из-за ON DELETE RESTRICT
        DAOException exception = assertThrows(DAOException.class, () -> {
            functionDAO.deleteById(componentFunc1Id);
        });

        // Проверяем, что элемент остался (транзакция не прошла)
        assertEquals(1, dao.selectAll(compositeFunctionId).size());
    }

    @Test
    void testInsertDuplicateOrder_ForbiddenBySchema() {
        dao.insert(new CompositeFunctionElement(compositeFunctionId, 1, componentFunc1Id));

        assertThrows(DAOException.class, () ->
                dao.insert(new CompositeFunctionElement(compositeFunctionId, 1, componentFunc2Id))
        );

        List<CompositeFunctionElement> elements = dao.selectAll(compositeFunctionId);
        assertEquals(1, elements.size());
    }

    @Test
    void testInsertWithNonExistentCompositeId_ForeignKeyViolation() {
        CompositeFunctionElement invalid = new CompositeFunctionElement(
                999999L, 1, componentFunc1Id);

        DAOException exception = assertThrows(DAOException.class, () -> dao.insert(invalid));
        assertTrue(exception.getCause() instanceof SQLException);
        assertTrue(exception.getMessage().contains("composite_function_elements"));
    }

    @Test
    void testMultipleComposites_Isolated() {
        // Создаём вторую композитную функцию
        Function composite2 = new Function(userId, "AnotherComposite", "COMPOSITE", "src");
        functionDAO.insert(composite2);
        long composite2Id = functionDAO.selectByUserId(userId).stream()
                .filter(f -> "AnotherComposite".equals(f.getName()))
                .findFirst().get().getId();

        dao.insert(new CompositeFunctionElement(compositeFunctionId, 1, componentFunc1Id));
        dao.insert(new CompositeFunctionElement(composite2Id, 1, componentFunc2Id));

        assertEquals(1, dao.selectAll(compositeFunctionId).size());
        assertEquals(1, dao.selectAll(composite2Id).size());
    }
}