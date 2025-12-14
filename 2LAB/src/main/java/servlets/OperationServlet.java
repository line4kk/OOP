package servlets;

import exceptions.AuthException;
import exceptions.DivisionByZeroException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.requests.OperationRequest;
import model.dto.responses.PointResponse;
import model.dto.responses.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.OperationService;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet(name = "OperationServlet", urlPatterns = "/operation")
public class OperationServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(OperationServlet.class);
    private final OperationService operationService = new OperationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserResponse user = authenticate(req);
            logger.info("POST /operation от пользователя {}", user.getId());
            OperationRequest requestBody = objectMapper.readValue(req.getInputStream(), OperationRequest.class);
            List<PointResponse> result = operationService.getOperationResult(user, requestBody);
            writeJson(resp, HttpServletResponse.SC_OK, result);
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при вызове /operation: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные при вызове /operation: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            logger.warn("Ресурс не найден при вызове /operation: {}", e.getMessage());
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (DivisionByZeroException e) {
            logger.warn("Ошибка деления на ноль при вызове /operation: {}", e.getMessage());
            writeError(resp, 418, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при вызове /operation", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}