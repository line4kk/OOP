
package servlets;

import exceptions.AuthException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.requests.UserLoginRequest;
import model.dto.responses.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(name = "UserAuthServlet", urlPatterns = "/users/auth")
public class UserAuthServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserAuthServlet.class);
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserLoginRequest loginRequest = objectMapper.readValue(req.getInputStream(), UserLoginRequest.class);
            logger.info("Попытка авторизации пользователя {}", loginRequest.getUsername());
            UserResponse response = userService.signIn(loginRequest);
            writeJson(resp, HttpServletResponse.SC_OK, response);
        } catch (AuthException e) {
            logger.warn("Неуспешная авторизация: {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные при авторизации: {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при авторизации", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}