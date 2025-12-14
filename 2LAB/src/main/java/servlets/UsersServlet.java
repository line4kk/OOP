package servlets;

import exceptions.AuthException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.responses.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.NoSuchElementException;

@WebServlet(name = "UsersServlet", urlPatterns = "/users")
public class UsersServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(UsersServlet.class);
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserResponse authenticated = authenticate(req);
            logger.info("GET /users для пользователя {}", authenticated.getId());
            UserResponse response = userService.getUserInfo(authenticated.getId());
            writeJson(resp, HttpServletResponse.SC_OK, response);
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при получении пользователя: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (NoSuchElementException e) {
            logger.warn("Пользователь не найден: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при получении данных пользователя", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
