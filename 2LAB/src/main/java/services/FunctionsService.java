package services;

import dao.CompositeFunctionElementDAO;
import dao.FunctionDAO;
import dao.FunctionPointDAO;
import exceptions.AlreadyExistsException;
import exceptions.MethodNotAllowedException;
import model.Function;
import model.FunctionPoint;
import model.dto.requests.*;
import model.dto.responses.FunctionResponse;
import model.dto.responses.PointResponse;
import model.enums.FunctionType;
import model.enums.SourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class FunctionsService extends AbstractService<FunctionDAO> {
    private final FunctionPointDAO pointDAO = new FunctionPointDAO();

    public FunctionsService() {
        super(new FunctionDAO());
    }

    public List<FunctionResponse> getUserFunctions(long userId) {
        List<Function> functions = dao.selectByUserId(userId);
        List<FunctionResponse> functionsResponses = new ArrayList<>();
        for (Function function : functions)
            functionsResponses.add(FunctionResponse.from(function));
        return functionsResponses;
    }

    public FunctionResponse createFunction(long userId, FunctionCreateRequest functionCreateRequest) {
        if (!SourceType.isValidSourceType(functionCreateRequest.getSource())
            || !FunctionType.isValidType(functionCreateRequest.getType())
        ) {
            throw new IllegalArgumentException("Bad Request");
        }

        Function function = functionCreateRequest.toEntity(userId);
        boolean isExists = dao.selectByUserId(userId).stream()
                .anyMatch(f -> f.getName().equals(functionCreateRequest.getName()));
        if (isExists) {
            throw new AlreadyExistsException("Функция с таким именем уже существует");
        }

        dao.insert(function);
        return FunctionResponse.from(function);
    }

    public List<FunctionResponse> getUserAnalyticFunctions(long userId) {
        logger.info("Запрос на получение всех аналитических функций пользователя userId={}", userId);

        List<Function> analyticFunctions = dao.selectByUserId(userId, "analytical");

        List<FunctionResponse> response = analyticFunctions.stream()
                .map(FunctionResponse::from)
                .toList();

        logger.info("У пользователя {} найдено {} аналитических функций", userId, response.size());
        return response;
    }

    public FunctionResponse createCompositeFunction(long userId, CompositeCreateRequest request) {
        logger.info("Создание композиционной аналитической функции пользователем {}: {}", userId, request);

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Ошибка. Некорректные данные.");
        }
        if (request.getFunctionIdsInOrder() == null || request.getFunctionIdsInOrder().isEmpty()) {
            throw new IllegalArgumentException("Ошибка. Некорректные данные.");
        }

        for (Long funcId : request.getFunctionIdsInOrder()) {
            Function f = dao.selectById(funcId);
            if (f == null) {
                throw new IllegalArgumentException("Ошибка. Некорректные данные.");
            }
        }

        boolean nameExists = dao.selectByUserId(userId).stream()
                .anyMatch(f -> f.getName().equals(request.getName()));
        if (nameExists) {
            throw new AlreadyExistsException("Композиция с таким именем уже существует");
        }

        Function compositeFunction = new Function(userId, request.getName(), "analytical", "composite");

        Function created = dao.insertWithElements(compositeFunction, request.getFunctionIdsInOrder());

        logger.info("Композиционная функция успешно создана: id={}, имя='{}'", created.getId(), created.getName());
        return FunctionResponse.from(created);
    }

    public FunctionResponse getFunction(long functionId) {
        Function function = dao.selectById(functionId);
        if (function != null) {
            return FunctionResponse.from(function);
        }
        throw new NoSuchElementException("Функция не найдена");
    }

    public FunctionResponse updateFunction(long userId, long functionId, FunctionCreateRequest updateRequest) {
        logger.info("Обновление функции id={} для пользователя userId={}", functionId, updateRequest);

        if (!SourceType.isValidSourceType(updateRequest.getSource())
                || !FunctionType.isValidType(updateRequest.getType())) {
            logger.warn("Некорректный тип функции или источник: type={}, source={}",
                    updateRequest.getType(), updateRequest.getSource());
            throw new IllegalArgumentException("Bad Request");
        }

        // Получаем текущую функцию
        Function existingFunction = dao.selectById(functionId);
        if (existingFunction == null) {
            logger.info("Функция с id={} не найдена", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        String newName = updateRequest.getName();

        // Проверяем, не занято ли новое имя у этого же пользователя (кроме текущей функции)
        boolean nameAlreadyUsed = dao.selectByUserId(userId).stream()
                .anyMatch(f -> f.getId() != functionId && f.getName().equals(newName));

        if (nameAlreadyUsed) {
            logger.info("Попытка переименовать функцию в уже существующее имя: {}", newName);
            throw new AlreadyExistsException("Функция с таким именем уже существует");
        }

        existingFunction.setName(newName);

        // Применяем изменения в базе
        if (newName != null && !newName.isBlank()) {
            dao.setName(functionId, newName);
        }

        logger.info("Функция id={} успешно обновлена: новое имя='{}'", functionId, existingFunction.getName());

        return FunctionResponse.from(existingFunction);
    }

    public void deleteFunction(long userId, long functionId) {
        logger.info("Запрос на удаление функции id={} от пользователя userId={}", functionId, userId);

        // Проверяем, существует ли функция
        Function function = dao.selectById(functionId);
        if (function == null) {
            logger.info("Попытка удалить несуществующую функцию id={}", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        // Проверяем, используется ли функция в какой-либо композиции
        CompositeFunctionElementDAO compositeElementDAO = new CompositeFunctionElementDAO();
        if (compositeElementDAO.isUsedInComposite(functionId)) {
            throw new MethodNotAllowedException("Невозможно удалить функцию, так как она используется в композиции");
        }

        pointDAO.deleteByFunctionId(functionId);

        dao.deleteById(functionId);

        logger.info("Функция id={} успешно удалена пользователем {}", functionId, userId);
    }

    public List<PointResponse> getFunctionPoints(long functionId) {
        if (dao.selectById(functionId) == null) {
            throw new NoSuchElementException("Функция не найдена");
        }
        List<FunctionPoint> functionPoints = pointDAO.selectByFunctionId(functionId);

        return PointResponse.fromList(functionPoints);
    }

    public List<PointResponse> addFunctionPoints(long functionId, List<PointRequest> pointRequests) {
        List<FunctionPoint> points = new ArrayList<>();
        for (PointRequest pr : pointRequests) {
            points.add(pr.toEntity(functionId));
        }
        pointDAO.insertList(points);
        // Ошибки обрабатываются прям ↑↑↑ там. (прошу прощения, я устал)
        return PointResponse.fromList(points);
    }

    public void deleteAllFunctionPoints(long userId, long functionId) {
        logger.info("Запрос на удаление всех точек функции id={} от пользователя userId={}", functionId, userId);

        Function function = dao.selectById(functionId);
        if (function == null) {
            logger.info("Попытка удалить точки несуществующей функции id={}", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        pointDAO.deleteByFunctionId(functionId);

        logger.info("Успешно удалены все точки функции id={} (пользователь {})", functionId, userId);
    }

    public PointResponse updateFunctionPointY(long userId, long functionId, long pointId, PointRequest pointRequest) {
        logger.info("Обновление Y-значения точки id={} функции id={} пользователем {}", pointId, functionId, userId);

        if (pointRequest.getY() == null) {
            logger.warn("Попытка обновить точку без указания Y: {}", pointRequest);
            throw new IllegalArgumentException("Ошибка. Проверьте, что у каждой точки есть значения x и y");
        }

        Function function = dao.selectById(functionId);
        if (function == null) {
            logger.info("Функция id={} не найдена при обновлении точки", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        FunctionPoint point = pointDAO.selectById(pointId);
        if (point == null) {
            logger.info("Точка с id={} не найдена", pointId);
            throw new NoSuchElementException("Точка не найдена");
        }

        pointDAO.updateYValue(functionId, point.getXValue(), pointRequest.getY());
        point.setYValue(pointRequest.getY());

        logger.info("Y-значение точки id={} успешно обновлено: y = {}", pointId, pointRequest.getY());
        return PointResponse.from(point);
    }

    public void deleteFunctionPoint(long userId, long functionId, long pointId) {
        logger.info("Удаление точки id={} из функции id={} пользователем {}", pointId, functionId, userId);

        Function function = dao.selectById(functionId);
        if (function == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        FunctionPoint point = pointDAO.selectById(pointId);
        if (point == null) {
            logger.info("Попытка удалить несуществующую точку id={}", pointId);
            throw new NoSuchElementException("Точка не найдена");
        }

        pointDAO.deleteById(pointId);

        logger.info("Точка id={} успешно удалена из функции id={}", pointId, functionId);
    }
}
