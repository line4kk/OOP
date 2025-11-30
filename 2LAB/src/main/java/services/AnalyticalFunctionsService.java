package services;

import dao.FunctionDAO;
import exceptions.AlreadyExistsException;
import model.Function;
import model.dto.requests.CompositeCreateRequest;
import model.dto.responses.FunctionResponse;

import java.util.List;

public class AnalyticalFunctionsService extends AbstractService<FunctionDAO> {
    public AnalyticalFunctionsService() {
        super(new FunctionDAO());
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

}
