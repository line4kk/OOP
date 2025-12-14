package servlets;

import exceptions.AlreadyExistsException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.requests.UserRegisterRequest;
import model.dto.responses.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(name = "UserRegisterServlet", urlPatterns = "/users/register")
public class UserRegisterServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserRegisterServlet.class);
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserRegisterRequest registerRequest = objectMapper.readValue(req.getInputStream(), UserRegisterRequest.class);
            logger.info("Попытка регистрации пользователя {}", registerRequest.getUsername());
            UserResponse response = userService.signUp(registerRequest);
            writeJson(resp, HttpServletResponse.SC_OK, response);
        } catch (AlreadyExistsException e) {
            logger.warn("Пользователь уже существует при регистрации: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные при регистрации: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при регистрации", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}