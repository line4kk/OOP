package util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private DatabaseConnection() throws SQLException {
        Dotenv dotenv = Dotenv.load();

        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        logger.debug("Считывание конфигурации базы данных из env");
        if (url == null || user == null || password == null) {
            logger.error("Отсутствует конфигурация базы данных в .env");
            throw new IllegalStateException("Отсутствует конфигурация базы данных в .env");
        }

        this.connection = DriverManager.getConnection(url, user, password);
    }

    public static synchronized Connection getConnection() {
        if (instance == null)
            try {
                instance = new DatabaseConnection();
            }
            catch (SQLException e) {
                logger.error("Ошибка при подключении к базе данных", e);
                throw new IllegalStateException("Ошибка при подключении к базе данных", e);
            }
        return instance.connection;
    }

}