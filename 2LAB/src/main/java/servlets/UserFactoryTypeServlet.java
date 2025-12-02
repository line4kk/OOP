package servlets;

import exceptions.AuthException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.responses.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(name = "UserFactoryTypeServlet", urlPatterns = "/users/settings/factory_types")
public class UserFactoryTypeServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserFactoryTypeServlet.class);

    private static class FactoryTypeRequest {
        private String factoryType;

        public String getFactoryType() {
            return factoryType;
        }

        public void setFactoryType(String factoryType) {
            this.factoryType = factoryType;
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserResponse authenticated = authenticate(req);
            logger.info("PUT /users/settings/factory_types от пользователя {}", authenticated.getId());
            FactoryTypeRequest request = objectMapper.readValue(req.getInputStream(), FactoryTypeRequest.class);
            UserResponse updated = userService.setFactoryType(authenticated.getId(), request.getFactoryType());
            writeJson(resp, HttpServletResponse.SC_OK, updated);
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при изменении фабрики: {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные при изменении фабрики: {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при изменении фабрики", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}