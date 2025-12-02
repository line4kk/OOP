package servlets;

import com.fasterxml.jackson.core.type.TypeReference;
import exceptions.AlreadyExistsException;
import exceptions.AuthException;
import exceptions.MethodNotAllowedException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.requests.FunctionCreateRequest;
import model.dto.requests.FunctionSamplingRequest;
import model.dto.requests.PointRequest;
import model.dto.responses.FunctionResponse;
import model.dto.responses.PointResponse;
import model.dto.responses.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.FunctionPointsService;
import services.FunctionsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@WebServlet(name = "FunctionDetailsServlet", urlPatterns = "/functions/*")
public class FunctionDetailsServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDetailsServlet.class);
    private final FunctionsService functionsService = new FunctionsService();
    private final FunctionPointsService functionPointsService = new FunctionPointsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            authenticate(req);
            List<String> segments = getPathSegments(req.getPathInfo());
            logger.info("GET {} с сегментами {}", req.getRequestURI(), segments);
            if (segments.size() == 1) {
                long functionId = parseId(segments.get(0));
                FunctionResponse response = functionsService.getFunction(functionId);
                writeJson(resp, HttpServletResponse.SC_OK, response);
            } else if (segments.size() == 2 && "points".equals(segments.get(1))) {
                long functionId = parseId(segments.get(0));
                List<PointResponse> points = functionPointsService.getFunctionPoints(functionId);
                writeJson(resp, HttpServletResponse.SC_OK, points);
            } else {
                logger.warn("Неизвестный путь при GET {}", req.getRequestURI());
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unsupported path");
            }
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при GET {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (NoSuchElementException e) {
            logger.warn("Ресурс не найден при GET {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (NumberFormatException e) {
            logger.warn("Некорректный идентификатор в пути {}", req.getRequestURI());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при обработке GET {}", req.getRequestURI(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserResponse user = authenticate(req);
            List<String> segments = getPathSegments(req.getPathInfo());
            logger.info("POST {} от пользователя {} с сегментами {}", req.getRequestURI(), user.getId(), segments);
            if (segments.size() == 2 && "points".equals(segments.get(1))) {
                long functionId = parseId(segments.get(0));
                List<PointRequest> points = objectMapper.readValue(req.getInputStream(), new TypeReference<List<PointRequest>>() {});
                List<PointResponse> created = functionPointsService.addFunctionPoints(functionId, points);
                writeJson(resp, HttpServletResponse.SC_OK, created);
            } else if (segments.size() == 2 && "sampling".equals(segments.get(1))) {
                long functionId = parseId(segments.get(0));
                FunctionSamplingRequest requestBody = objectMapper.readValue(req.getInputStream(), FunctionSamplingRequest.class);
                if (requestBody.getFunctionId() == 0) {
                    requestBody.setFunctionId(functionId);
                }
                functionsService.getFunction(functionId);
                List<PointResponse> created = functionPointsService.addFunctionPointsBySampling(requestBody);
                writeJson(resp, HttpServletResponse.SC_OK, created);
            } else {
                logger.warn("Неизвестный путь при POST {}", req.getRequestURI());
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unsupported path");
            }
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при POST {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные при POST {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (AlreadyExistsException e) {
            logger.info("Попытка создать существующий ресурс при POST {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (NoSuchElementException e) {
            logger.warn("Ресурс не найден при POST {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при обработке POST {}", req.getRequestURI(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserResponse user = authenticate(req);
            List<String> segments = getPathSegments(req.getPathInfo());
            logger.info("PUT {} от пользователя {} с сегментами {}", req.getRequestURI(), user.getId(), segments);
            if (segments.size() == 1) {
                long functionId = parseId(segments.get(0));
                FunctionCreateRequest updateRequest = objectMapper.readValue(req.getInputStream(), FunctionCreateRequest.class);
                FunctionResponse updated = functionsService.updateFunction(user.getId(), functionId, updateRequest);
                writeJson(resp, HttpServletResponse.SC_OK, updated);
            } else if (segments.size() == 3 && "points".equals(segments.get(1))) {
                long functionId = parseId(segments.get(0));
                long pointId = parseId(segments.get(2));
                PointRequest requestBody = objectMapper.readValue(req.getInputStream(), PointRequest.class);
                PointResponse updated = functionPointsService.updateFunctionPointY(user.getId(), functionId, pointId, requestBody);
                writeJson(resp, HttpServletResponse.SC_OK, updated);
            } else {
                logger.warn("Неизвестный путь при PUT {}", req.getRequestURI());
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unsupported path");
            }
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при PUT {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные при PUT {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (AlreadyExistsException e) {
            logger.info("Попытка создать существующий ресурс при PUT {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (NoSuchElementException e) {
            logger.warn("Ресурс не найден при PUT {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при обработке PUT {}", req.getRequestURI(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UserResponse user = authenticate(req);
            List<String> segments = getPathSegments(req.getPathInfo());
            logger.info("DELETE {} от пользователя {} с сегментами {}", req.getRequestURI(), user.getId(), segments);
            if (segments.size() == 1) {
                long functionId = parseId(segments.get(0));
                functionsService.deleteFunction(user.getId(), functionId);
                writeJson(resp, HttpServletResponse.SC_OK, null);
            } else if (segments.size() == 2 && "points".equals(segments.get(1))) {
                long functionId = parseId(segments.get(0));
                functionPointsService.deleteAllFunctionPoints(user.getId(), functionId);
                writeJson(resp, HttpServletResponse.SC_OK, null);
            } else if (segments.size() == 3 && "points".equals(segments.get(1))) {
                long functionId = parseId(segments.get(0));
                long pointId = parseId(segments.get(2));
                functionPointsService.deleteFunctionPoint(user.getId(), functionId, pointId);
                writeJson(resp, HttpServletResponse.SC_OK, null);
            } else {
                logger.warn("Неизвестный путь при DELETE {}", req.getRequestURI());
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unsupported path");
            }
        } catch (AuthException e) {
            logger.warn("Доступ запрещен при DELETE {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (NoSuchElementException e) {
            logger.warn("Ресурс не найден при DELETE {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (MethodNotAllowedException e) {
            logger.warn("Операция не разрешена при DELETE {}: {}", req.getRequestURI(), e.getMessage());
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getMessage());
        } catch (NumberFormatException e) {
            logger.warn("Некорректный идентификатор в пути {}", req.getRequestURI());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при обработке DELETE {}", req.getRequestURI(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private List<String> getPathSegments(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(pathInfo.split("/"))
                .filter(segment -> !segment.isBlank())
                .toList();
    }

    private long parseId(String value) {
        return Long.parseLong(value);
    }
}