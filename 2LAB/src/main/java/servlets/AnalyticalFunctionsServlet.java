package servlets;

import exceptions.AlreadyExistsException;
import exceptions.AuthException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.requests.CompositeCreateRequest;
import model.dto.responses.FunctionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.AnalyticalFunctionsService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AnalyticalFunctionsServlet", urlPatterns = "/functions/analytical_functions")
public class AnalyticalFunctionsServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticalFunctionsServlet.class);
    private final AnalyticalFunctionsService analyticalFunctionsService = new AnalyticalFunctionsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var user = authenticate(req);
            logger.info("Получен запрос GET /functions/analytical_functions от пользователя {}", user.getId());
            List<FunctionResponse> functions = analyticalFunctionsService.getUserAnalyticFunctions(user.getId());
            writeJson(resp, HttpServletResponse.SC_OK, functions);
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при запросе аналитических функций: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при получении аналитических функций", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var user = authenticate(req);
            logger.info("Получен запрос POST /functions/analytical_functions от пользователя {}", user.getId());
            CompositeCreateRequest request = objectMapper.readValue(req.getInputStream(), CompositeCreateRequest.class);
            FunctionResponse created = analyticalFunctionsService.createCompositeFunction(user.getId(), request);
            writeJson(resp, HttpServletResponse.SC_OK, created);
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при создании аналитической функции: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные при создании аналитической функции: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (AlreadyExistsException e) {
            logger.info("Попытка создать существующую аналитическую функцию: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при создании аналитической функции", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}