package engine.controller;

import engine.dto.*;
import engine.entity.CompositeFunctionElements;
import engine.entity.Functions;
import engine.entity.FunctionPoints;
import engine.entity.Users;
import engine.repository.CompositeFunctionElementsRepository;
import engine.repository.FunctionsRepository;
import engine.repository.FunctionPointsRepository;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import functions.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@RestController
public class AnalyticalFunctionController {

    @Autowired private MultipleSearchService multipleSearchService;
    @Autowired private SingleSearchService singleSearchService;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;

    private static final Logger logger = LoggerFactory.getLogger(AnalyticalFunctionController.class);

    @PostMapping("/analytical_functions")
    @Transactional
    public ResponseEntity<?> createCompositeFunction(@RequestBody CompositeCreateRequest request) {
        try {
            logger.info("POST /analytical_functions - создание композитной функции: {}", request.getName());

            if (request.getFunctionIdsInOrder() == null || request.getFunctionIdsInOrder().size() < 2) {
                return ResponseEntity.status(400).body("Ошибка. Некорректные данные: требуется минимум 2 функции");
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя функции не может быть пустым");
            }
            if (request.getName().length() > 50) {
                return ResponseEntity.status(400).body("Имя функции не может превышать 50 символов");
            }

            for (Long functionId : request.getFunctionIdsInOrder()) {
                if (functionId == null || functionId <= 0) {
                    return ResponseEntity.status(400).body("Некорректный ID функции в списке");
                }
            }

            List<Functions> existingFunctions = multipleSearchService.findFunctionsByName(request.getName());
            if (!existingFunctions.isEmpty()) {
                return ResponseEntity.status(409).body("Композиция с таким именем уже существует");
            }

            List<Users> users = multipleSearchService.findAllUsers();
            if (users.isEmpty()) {
                return ResponseEntity.status(400).body("Нет пользователей в системе");
            }
            Users currentUser = users.get(0);

            Functions newFunction = new Functions(currentUser, request.getName(), "analytical", "composite");
            Functions savedFunction = functionsRepository.save(newFunction);

            logger.debug("Создана композитная функция: ID={}, имя='{}'",
                    savedFunction.getId(), savedFunction.getName());

            for (int i = 0; i < request.getFunctionIdsInOrder().size(); i++) {
                Long componentId = request.getFunctionIdsInOrder().get(i);
                Functions componentFunction = singleSearchService.findFunctionById(componentId)
                        .orElseThrow(() -> new IllegalArgumentException("Функция с ID " + componentId + " не найдена"));

                CompositeFunctionElements element = new CompositeFunctionElements(savedFunction, i, componentFunction);
                compositeFunctionElementsRepository.save(element);

                logger.debug("Добавлен элемент композиции: порядок={}, функция='{}' (ID: {})",
                        i, componentFunction.getName(), componentFunction.getId());
            }

            logger.info("Успешно создана композитная функция '{}' с {} компонентами",
                    savedFunction.getName(), request.getFunctionIdsInOrder().size());

            FunctionResponse response = new FunctionResponse(
                    savedFunction.getId(),
                    savedFunction.getName(),
                    savedFunction.getType(),
                    savedFunction.getSource()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании композитной функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/functions/{function_id}/sampling")
    @Transactional
    public ResponseEntity<?> addFunctionPointsBySampling(
            @PathVariable("function_id") Long functionId,
            @RequestBody SamplingRequest request) {
        try {
            logger.info("POST /functions/{}/sampling - сэмплирование аналитической функции: от {} до {}, {} точек",
                    functionId, request.getXFrom(), request.getXTo(), request.getCount());

            if (functionId == null || functionId <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }

            if (request.getXFrom() == null || request.getXTo() == null) {
                return ResponseEntity.status(400).body("Некорректные данные: x_from и x_to обязательны");
            }
            if (request.getXFrom() >= request.getXTo()) {
                return ResponseEntity.status(400).body("x_from должен быть меньше x_to");
            }
            if (request.getCount() == null || request.getCount() <= 0) {
                return ResponseEntity.status(400).body("Количество точек должно быть положительным числом");
            }

            Functions function = singleSearchService.findFunctionById(functionId).orElse(null);
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!"analytical".equals(function.getType())) {
                return ResponseEntity.status(400).body("Сэмплирование доступно только для аналитических функций");
            }

            List<PointResponse> response = new ArrayList<>();
            double step = (request.getXTo() - request.getXFrom()) / (request.getCount() - 1);

            for (int i = 0; i < request.getCount(); i++) {
                double x = request.getXFrom() + (i * step);
                double y = calculateAnalyticalFunctionValue(function, x);

                boolean pointExists = function.getPoints().stream()
                        .anyMatch(existingPoint -> Math.abs(existingPoint.getXValue() - x) < 1e-10);

                if (!pointExists) {
                    FunctionPoints newPoint = new FunctionPoints(function, x, y);
                    FunctionPoints savedPoint = functionPointsRepository.save(newPoint);
                    response.add(new PointResponse(
                            savedPoint.getFunction().getId(),
                            savedPoint.getXValue(),
                            savedPoint.getYValue()
                    ));
                }
            }

            logger.info("Сэмплирование завершено. Добавлено {} точек для функции '{}'",
                    response.size(), function.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при сэмплировании аналитической функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/analytical_functions/{function_id}/compute")
    public ResponseEntity<?> computeAnalyticalFunction(
            @PathVariable("function_id") Long functionId,
            @RequestBody ComputeRequest request) {
        try {
            logger.info("POST /analytical_functions/{}/compute - вычисление значения для x={}",
                    functionId, request.getX());

            if (functionId == null || functionId <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }

            if (request.getX() == null) {
                return ResponseEntity.status(400).body("Значение x обязательно");
            }

            Functions function = singleSearchService.findFunctionById(functionId).orElse(null);
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!"analytical".equals(function.getType())) {
                return ResponseEntity.status(400).body("Вычисление доступно только для аналитических функций");
            }

            double result = calculateAnalyticalFunctionValue(function, request.getX());

            ComputeResponse response = new ComputeResponse();
            response.setResult(result);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при вычислении аналитической функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/analytical_functions/{function_id}/composition")
    public ResponseEntity<?> getFunctionComposition(@PathVariable("function_id") Long functionId) {
        try {
            logger.info("GET /analytical_functions/{}/composition - получение состава функции", functionId);

            if (functionId == null || functionId <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }

            Functions function = singleSearchService.findFunctionById(functionId).orElse(null);
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!"composite".equals(function.getSource())) {
                return ResponseEntity.status(400).body("Функция не является композитной");
            }

            List<CompositeFunctionElements> composition = compositeFunctionElementsRepository
                    .findByComposite(function);
            composition.sort((a, b) -> a.getFunctionOrder().compareTo(b.getFunctionOrder()));

            List<CompositeElementResponse> response = composition.stream()
                    .map(element -> new CompositeElementResponse(
                            element.getId(),
                            element.getFunctionOrder(),
                            element.getFunction().getId()
                    ))
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении состава функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private Double calculateAnalyticalFunctionValue(Functions analyticalFunction, double x) {
        try {
            if ("composite".equals(analyticalFunction.getSource())) {
                return calculateCompositeFunction(analyticalFunction, x);
            }
            return calculateBasicFunction(analyticalFunction.getName(), x);
        } catch (Exception e) {
            logger.error("Ошибка вычисления функции '{}' для x={}", analyticalFunction.getName(), x, e);
            return Double.NaN;
        }
    }

    private Double calculateCompositeFunction(Functions compositeFunction, double x) {
        try {
            logger.debug("Вычисление композитной функции '{}' (ID: {}) для x={}",
                    compositeFunction.getName(), compositeFunction.getId(), x);

            List<CompositeFunctionElements> composition = compositeFunctionElementsRepository
                    .findByComposite(compositeFunction);
            composition.sort((a, b) -> a.getFunctionOrder().compareTo(b.getFunctionOrder()));

            if (composition == null || composition.isEmpty()) {
                logger.error("Композитная функция '{}' не имеет элементов состава", compositeFunction.getName());
                return Double.NaN;
            }

            double result = x;
            for (CompositeFunctionElements element : composition) {
                Functions componentFunction = element.getFunction();
                if (componentFunction == null) {
                    logger.error("Элемент композитной функции имеет null function");
                    return Double.NaN;
                }

                result = calculateAnalyticalFunctionValue(componentFunction, result);
                if (Double.isNaN(result)) {
                    break;
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("Ошибка вычисления композитной функции '{}'", compositeFunction.getName(), e);
            return Double.NaN;
        }
    }

    private Double calculateBasicFunction(String functionName, double x) {
        if (functionName == null) return Double.NaN;

        switch (functionName.toLowerCase()) {
            case "sin": case "синус": return Math.sin(x);
            case "cos": case "косинус": return Math.cos(x);
            case "tan": case "tangent": case "тангенс": return Math.tan(x);
            case "exp": case "exponential": case "экспонента": return Math.exp(x);
            case "log": case "logarithm": case "логарифм": return x > 0 ? Math.log(x) : Double.NaN;
            case "sqrt": case "square_root": case "квадратный_корень": return x >= 0 ? Math.sqrt(x) : Double.NaN;
            case "quadratic": case "квадратичная": case "sqr": case "square":
                return new SqrFunction().apply(x);
            case "identity": case "тождественная": case "x":
                return new IdentityFunction().apply(x);
            case "zero": case "ноль": return new ZeroFunction().apply(x);
            case "unit": case "единичная": return new UnitFunction().apply(x);
            default:
                logger.warn("Неизвестная функция '{}', используется тождественная", functionName);
                return new IdentityFunction().apply(x);
        }
    }

    @Data
    public static class ComputeRequest {
        private Double x;
    }

    @Data
    public static class ComputeResponse {
        private Double result;
    }

    @Data
    public static class SamplingRequest {
        private Long analyticalFunctionId;
        private Double xFrom;
        private Double xTo;
        private Integer count;
    }
}