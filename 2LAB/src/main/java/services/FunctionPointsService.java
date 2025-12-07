package services;

import dao.FunctionDAO;
import dao.FunctionPointDAO;
import dao.UserDAO;
import functions.MathFunction;
import functions.Point;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import model.Function;
import model.FunctionPoint;
import model.User;
import model.dto.requests.FunctionSamplingRequest;
import model.dto.requests.PointRequest;
import model.dto.responses.PointResponse;
import model.dto.responses.UserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class FunctionPointsService extends AbstractService<FunctionPointDAO> {
    private final FunctionDAO functionsDAO = new FunctionDAO();
    private final UserDAO userDAO = new UserDAO();
    public FunctionPointsService() {
        super(new FunctionPointDAO());
    }

    public List<PointResponse> getFunctionPoints(UserResponse user, long functionId) {
        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        ensureAccess(user, function.getUserId());
        List<FunctionPoint> functionPoints = dao.selectByFunctionId(functionId);

        return PointResponse.fromList(functionPoints);
    }

    public List<PointResponse> addFunctionPoints(UserResponse user, long functionId, List<PointRequest> pointRequests) {
        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        ensureAccess(user, function.getUserId());
        List<FunctionPoint> points = new ArrayList<>();
        for (PointRequest pr : pointRequests) {
            points.add(pr.toEntity(functionId));
        }
        dao.insertList(points);
        // Ошибки обрабатываются прям ↑↑↑ там. (прошу прощения, я устал)
        return PointResponse.fromList(points);
    }

    public List<PointResponse> addFunctionPointsBySampling(UserResponse user, FunctionSamplingRequest request) {
        Function functionToCreate = functionsDAO.selectById(request.getFunctionId());
        if (functionToCreate == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        ensureAccess(user, functionToCreate.getUserId());
        User owner = userDAO.select(functionToCreate.getUserId());

        if (owner == null) {
            logger.error("Пользователь {} не найден", functionToCreate.getUserId());
            throw new NoSuchElementException("Пользователь не найден");
        }

        TabulatedFunctionFactory factory;
        switch (owner.getFactoryType()) {
            case "linked_list" -> factory = new LinkedListTabulatedFunctionFactory();
            case "array" -> factory = new ArrayTabulatedFunctionFactory();
            default -> {
                logger.error("Неизвестный тип фабрики у пользователя {}: {}", owner.getId(), owner.getFactoryType());
                throw new IllegalStateException("Некорректная фабрика пользователя");
            }
        }

        AnalyticalFunctionsService service = new AnalyticalFunctionsService();
        List<String> analyticalFunctionNames = service.toAnalyticalFunctionNames(
                request.getAnalyticalFunctionId()
        );
        MathFunction source = composeAnalyticalFunction(analyticalFunctionNames);

        try {
            TabulatedFunction createdFunction = factory.create(source, request.getXFrom(), request.getXTo(), request.getCount());
            List<FunctionPoint> functionPoints = new ArrayList<>();
            for (Point point : createdFunction) {
                FunctionPoint functionPoint = new FunctionPoint(request.getFunctionId(), point.x, point.y);
                functionPoints.add(functionPoint);
            }
            dao.insertList(functionPoints);
            return PointResponse.fromList(functionPoints);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при создании TabulatedFunction при использовании семплирования: {}", request, e);
            throw new IllegalArgumentException("Некорректные данные.");
        }

    }

    private MathFunction composeAnalyticalFunction(List<String> functionNames) {
        if (functionNames == null || functionNames.isEmpty()) {
            throw new IllegalArgumentException("Не указаны аналитические функции для табулирования");
        }

        MathFunction result = AnalyticalFunctionRegistry.getFunction(functionNames.get(0));
        for (int i = 1; i < functionNames.size(); i++) {
            result = result.andThen(AnalyticalFunctionRegistry.getFunction(functionNames.get(i)));
        }
        return result;
    }

    public void deleteAllFunctionPoints(UserResponse user, long functionId) {
        logger.info("Запрос на удаление всех точек функции id={} от пользователя userId={}", functionId, user.getId());

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            logger.info("Попытка удалить точки несуществующей функции id={}", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        ensureAccess(user, function.getUserId());

        dao.deleteByFunctionId(functionId);

        logger.info("Успешно удалены все точки функции id={} (пользователь {})", functionId, user.getId());
    }

    public PointResponse updateFunctionPointY(UserResponse user, long functionId, long pointId, PointRequest pointRequest) {
        logger.info("Обновление Y-значения точки id={} функции id={} пользователем {}", pointId, functionId, user.getId());

        if (pointRequest.getY() == null) {
            logger.warn("Попытка обновить точку без указания Y: {}", pointRequest);
            throw new IllegalArgumentException("Ошибка. Проверьте, что у каждой точки есть значения x и y");
        }

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            logger.info("Функция id={} не найдена при обновлении точки", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        ensureAccess(user, function.getUserId());

        FunctionPoint point = dao.selectById(pointId);
        if (point == null) {
            logger.info("Точка с id={} не найдена", pointId);
            throw new NoSuchElementException("Точка не найдена");
        }

        dao.updateYValue(functionId, point.getXValue(), pointRequest.getY());
        point.setYValue(pointRequest.getY());

        logger.info("Y-значение точки id={} успешно обновлено: y = {}", pointId, pointRequest.getY());
        return PointResponse.from(point);
    }

    public void deleteFunctionPoint(UserResponse user, long functionId, long pointId) {
        logger.info("Удаление точки id={} из функции id={} пользователем {}", pointId, functionId, user.getId());

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        ensureAccess(user, function.getUserId());

        FunctionPoint point = dao.selectById(pointId);
        if (point == null) {
            logger.info("Попытка удалить несуществующую точку id={}", pointId);
            throw new NoSuchElementException("Точка не найдена");
        }

        dao.deleteById(pointId);

        logger.info("Точка id={} успешно удалена из функции id={}", pointId, functionId);
    }
}