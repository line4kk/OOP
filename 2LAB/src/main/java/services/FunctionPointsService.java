package services;

import dao.FunctionDAO;
import dao.FunctionPointDAO;
import dao.UserDAO;
import registry.AnalyticalFunctionRegistry;
import functions.MathFunction;
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class FunctionPointsService extends AbstractService<FunctionPointDAO> {
    private final FunctionDAO functionsDAO = new FunctionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AnalyticalFunctionsService analyticalFunctionsService = new AnalyticalFunctionsService();

    public FunctionPointsService() {
        super(new FunctionPointDAO());
    }

    public List<PointResponse> getFunctionPoints(long functionId) {
        if (functionsDAO.selectById(functionId) == null) {
            throw new NoSuchElementException("Функция не найдена");
        }
        List<FunctionPoint> functionPoints = dao.selectByFunctionId(functionId);

        return PointResponse.fromList(functionPoints);
    }

    public List<PointResponse> addFunctionPoints(long functionId, List<PointRequest> pointRequests) {
        List<FunctionPoint> points = new ArrayList<>();
        for (PointRequest pr : pointRequests) {
            points.add(pr.toEntity(functionId));
        }
        dao.insertList(points);
        // Ошибки обрабатываются прям ↑↑↑ там. (прошу прощения, я устал)
        return PointResponse.fromList(points);
    }

    public List<PointResponse> addFunctionPointsBySampling(FunctionSamplingRequest request) {
        logger.info("Создание точек для функции {} через табулирование {}", request.getFunctionId(), request);

        validateSamplingRequest(request);

        Function function = functionsDAO.selectById(request.getFunctionId());
        if (function == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        List<String> analyticalFunctions = analyticalFunctionsService.toAnalyticalFunctionNames(
                request.getAnalyticalFunctionId()
        );
        MathFunction composedFunction = composeAnalyticalFunction(analyticalFunctions);

        TabulatedFunctionFactory factory = resolveFactory(function.getUserId());
        TabulatedFunction tabulatedFunction = factory.create(
                composedFunction,
                request.getXFrom(),
                request.getXTo(),
                request.getCount()
        );

        List<FunctionPoint> points = new ArrayList<>();
        for (int i = 0; i < tabulatedFunction.getCount(); i++) {
            points.add(new FunctionPoint(
                    request.getFunctionId(),
                    tabulatedFunction.getX(i),
                    tabulatedFunction.getY(i)
            ));
        }

        dao.insertList(points);
        return PointResponse.fromList(points);
    }

    private void validateSamplingRequest(FunctionSamplingRequest request) {
        if (request == null || request.getFunctionId() == 0 || request.getAnalyticalFunctionId() == 0
                || request.getXFrom() == null || request.getXTo() == null || request.getCount() == null) {
            throw new IllegalArgumentException("Ошибка. Некорректные данные.");
        }
        if (request.getCount() <= 1) {
            throw new IllegalArgumentException("Количество точек должно быть больше 1");
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

    private TabulatedFunctionFactory resolveFactory(long userId) {
        User user = userDAO.select(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        return switch (user.getFactoryType()) {
            case null, "array" -> new ArrayTabulatedFunctionFactory();
            case "linked_list" -> new LinkedListTabulatedFunctionFactory();
            default -> throw new IllegalArgumentException("Некорректный тип фабрики: " + user.getFactoryType());
        };
    }

    public void deleteAllFunctionPoints(long userId, long functionId) {
        logger.info("Запрос на удаление всех точек функции id={} от пользователя userId={}", functionId, userId);

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            logger.info("Попытка удалить точки несуществующей функции id={}", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        dao.deleteByFunctionId(functionId);

        logger.info("Успешно удалены все точки функции id={} (пользователь {})", functionId, userId);
    }

    public PointResponse updateFunctionPointY(long userId, long functionId, long pointId, PointRequest pointRequest) {
        logger.info("Обновление Y-значения точки id={} функции id={} пользователем {}", pointId, functionId, userId);

        if (pointRequest.getY() == null) {
            logger.warn("Попытка обновить точку без указания Y: {}", pointRequest);
            throw new IllegalArgumentException("Ошибка. Проверьте, что у каждой точки есть значения x и y");
        }

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            logger.info("Функция id={} не найдена при обновлении точки", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

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

    public void deleteFunctionPoint(long userId, long functionId, long pointId) {
        logger.info("Удаление точки id={} из функции id={} пользователем {}", pointId, functionId, userId);

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        FunctionPoint point = dao.selectById(pointId);
        if (point == null) {
            logger.info("Попытка удалить несуществующую точку id={}", pointId);
            throw new NoSuchElementException("Точка не найдена");
        }

        dao.deleteById(pointId);

        logger.info("Точка id={} успешно удалена из функции id={}", pointId, functionId);
    }
}
