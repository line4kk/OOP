package dao;

import util.DatabaseConnection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Общая инициализация in-memory базы для DAO-тестов.
 */
final class DaoTestSupport {

    private static boolean initialized = false;

    private DaoTestSupport() {
    }

    static synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }
        Connection connection = DatabaseConnection.getConnection();
        try {
            for (String script : schemaScripts()) {
                runScript(connection, script);
            }
            initialized = true;
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Не удалось инициализировать тестовую БД", e);
        }
    }

    static void clearAllTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.addBatch("DELETE FROM operations_result_points");
            statement.addBatch("DELETE FROM composite_function_elements");
            statement.addBatch("DELETE FROM function_points");
            statement.addBatch("DELETE FROM functions");
            statement.addBatch("DELETE FROM users");
            statement.executeBatch();
        }
    }

    private static List<String> schemaScripts() {
        return List.of(
                "/scripts/create_users.sql",
                "/scripts/create_functions.sql",
                "/scripts/create_function_points.sql",
                "/scripts/create_composite_function_elements.sql",
                "/scripts/create_operations_result_points.sql"
        );
    }

    private static void runScript(Connection connection, String resourcePath) throws IOException, SQLException {
        try (InputStream is = DaoTestSupport.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Скрипт не найден: " + resourcePath);
            }

            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql); // ← PostgreSQL позволяет выполнять много операторов одним вызовом
            }
        }
    }
}