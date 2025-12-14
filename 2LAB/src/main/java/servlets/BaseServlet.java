package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import exceptions.AuthException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.requests.UserLoginRequest;
import model.dto.responses.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class BaseServlet extends HttpServlet {
    protected final Logger logger;
    protected final ObjectMapper objectMapper;
    protected final UserService userService;

    protected BaseServlet() {
        this.logger = LoggerFactory.getLogger(getClass());
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.findAndRegisterModules();
        this.userService = new UserService();
    }

    protected UserResponse authenticate(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            logger.warn("Не передан заголовок Authorization при обращении по пути {}", request.getRequestURI());
            throw new AuthException("Forbidden");
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.warn("Не удалось декодировать заголовок Authorization: {}", e.getMessage());
            throw new AuthException("Forbidden");
        }
        int separatorIndex = decoded.indexOf(':');
        if (separatorIndex < 0) {
            logger.warn("Некорректный формат учетных данных в Authorization");
            throw new AuthException("Forbidden");
        }

        String username = decoded.substring(0, separatorIndex);
        String password = decoded.substring(separatorIndex + 1);
        try {
            UserResponse authenticated = userService.signIn(new UserLoginRequest(username, password));
            logger.info("Пользователь {} успешно аутентифицирован", username);
            return authenticated;
        } catch (AuthException e) {
            logger.warn("Неуспешная попытка аутентификации пользователя {}", username);
            throw e;
        }
    }

    protected void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        logger.debug("Отправка ответа со статусом {}", status);
        if (body != null) {
            objectMapper.writeValue(response.getWriter(), body);
        }
    }

    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        logger.debug("Отправка ошибки со статусом {}: {}", status, message);
        if (message != null) {
            response.getWriter().write(message);
        }
    }
}