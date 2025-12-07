package services;

import dao.CompositeFunctionElementDAO;
import dao.FunctionDAO;
import dao.FunctionPointDAO;
import exceptions.AlreadyExistsException;
import exceptions.MethodNotAllowedException;
import model.Function;
import model.dto.requests.*;
import model.dto.responses.FunctionResponse;
import model.dto.responses.UserResponse;
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

        function = dao.insert(function);
        return FunctionResponse.from(function);
    }

    public FunctionResponse getFunction(UserResponse user, long functionId) {
        Function function = dao.selectById(functionId);
        if (function == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        ensureAccess(user, function.getUserId());
        return FunctionResponse.from(function);
    }

    public FunctionResponse updateFunction(UserResponse user, long functionId, FunctionCreateRequest updateRequest) {
        logger.info("Обновление функции id={} для пользователя userId={}", functionId, user.getId());

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

        ensureAccess(user, existingFunction.getUserId());

        String newName = updateRequest.getName();

        // Проверяем, не занято ли новое имя у этого же пользователя (кроме текущей функции)
        boolean nameAlreadyUsed = dao.selectByUserId(existingFunction.getUserId()).stream()
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

    public void deleteFunction(UserResponse user, long functionId) {
        logger.info("Запрос на удаление функции id={} от пользователя userId={}", functionId, user.getId());

        // Проверяем, существует ли функция
        Function function = dao.selectById(functionId);
        if (function == null) {
            logger.info("Попытка удалить несуществующую функцию id={}", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        ensureAccess(user, function.getUserId());

        // Проверяем, используется ли функция в какой-либо композиции
        CompositeFunctionElementDAO compositeElementDAO = new CompositeFunctionElementDAO();
        if (compositeElementDAO.isUsedInComposite(functionId)) {
            throw new MethodNotAllowedException("Невозможно удалить функцию, так как она используется в композиции");
        }

        pointDAO.deleteByFunctionId(functionId);

        dao.deleteById(functionId);

        logger.info("Функция id={} успешно удалена пользователем {}", functionId, user.getId());
    }
}