
package servlets;

import exceptions.AlreadyExistsException;
import exceptions.AuthException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.requests.FunctionCreateRequest;
import model.dto.responses.FunctionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.FunctionsService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "FunctionsServlet", urlPatterns = "/functions")
public class FunctionsServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsServlet.class);
    private final FunctionsService functionsService = new FunctionsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var user = authenticate(req);
            logger.info("Получен запрос GET /functions от пользователя {}", user.getId());
            List<FunctionResponse> functions = functionsService.getUserFunctions(user.getId());
            writeJson(resp, HttpServletResponse.SC_OK, functions);
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при запросе GET /functions: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при получении функций", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var user = authenticate(req);
            logger.info("Получен запрос POST /functions от пользователя {}", user.getId());
            FunctionCreateRequest request = objectMapper.readValue(req.getInputStream(), FunctionCreateRequest.class);
            FunctionResponse created = functionsService.createFunction(user.getId(), request);
            writeJson(resp, HttpServletResponse.SC_OK, created);
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при создании функции: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные при создании функции: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (AlreadyExistsException e) {
            logger.info("Попытка создать уже существующую функцию: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при создании функции", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}