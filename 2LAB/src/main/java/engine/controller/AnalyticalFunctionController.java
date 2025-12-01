package engine.controller;

import engine.dto.*;
import engine.entity.CompositeFunctionElements;
import engine.entity.Functions;
import engine.entity.FunctionPoints;
import engine.entity.Users;
import engine.repository.CompositeFunctionElementsRepository;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
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
import java.util.Optional;

@RestController
public class AnalyticalFunctionController {

    @Autowired private MultipleSearchService multipleSearchService;
    @Autowired private SingleSearchService singleSearchService;
    @Autowired private SecurityUtils securityUtils;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    private static final Logger logger = LoggerFactory.getLogger(AnalyticalFunctionController.class);

    @PostMapping("/functions/analytical_functions")
    @Transactional
    public ResponseEntity<?> createCompositeFunction(@RequestBody CompositeCreateRequest request) {
        try {
            logger.info("POST /functions/analytical_functions - создание композитной функции: {}", request.getName());

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getFunctionIdsInOrder() == null || request.getFunctionIdsInOrder().size() < 2) {
                return ResponseEntity.status(400).body("Ошибка. Некорректные данные: требуется минимум 2 функции");
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя функции не может быть пустым");
            }

            for (Long function_id : request.getFunctionIdsInOrder()) {
                if (function_id == null || function_id <= 0) {
                    return ResponseEntity.status(400).body("Некорректный ID функции в списке");
                }
            }

            List<Functions> existing_functions = multipleSearchService.findFunctionsByName(request.getName());
            boolean name_exists = existing_functions.stream()
                    .anyMatch(f -> f.getUser().getId().equals(current_user.getId()));
            if (name_exists) {
                return ResponseEntity.status(409).body("Композиция с таким именем уже существует");
            }

            Functions new_function = new Functions(current_user, request.getName(), "analytical", "composite");
            Functions saved_function = singleSearchService.saveFunction(new_function);

            for (int i = 0; i < request.getFunctionIdsInOrder().size(); i++) {
                Long componentId = request.getFunctionIdsInOrder().get(i);
                Optional<Functions> componentFunctionOpt = singleSearchService.findFunctionById(componentId);
                if (componentFunctionOpt.isEmpty()) {
                    return ResponseEntity.status(400).body("Функция с ID " + componentId + " не найдена");
                }

                Functions componentFunction = componentFunctionOpt.get();
                if (!componentFunction.getUser().getId().equals(current_user.getId())) {
                    return ResponseEntity.status(403).body("Forbidden");
                }

                CompositeFunctionElements element = new CompositeFunctionElements(saved_function, i, componentFunction);
                compositeFunctionElementsRepository.save(element);
            }

            FunctionResponse response = new FunctionResponse(
                    saved_function.getId(),
                    saved_function.getName(),
                    saved_function.getType(),
                    saved_function.getSource()
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
            @PathVariable("function_id") Long function_id,
            @RequestBody FunctionSamplingRequest request) {
        try {
            logger.info("POST /functions/{}/sampling - сэмплирование аналитической функции", function_id);

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (function_id == null || function_id <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }

            Optional<Functions> function_opt = singleSearchService.findFunctionById(function_id);
            if (function_opt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            Functions function = function_opt.get();
            if (!function.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getX_from() == null || request.getX_to() == null) {
                return ResponseEntity.status(400).body("Некорректные данные: x_from и x_to обязательны");
            }
            if (request.getX_from() >= request.getX_to()) {
                return ResponseEntity.status(400).body("x_from должен быть меньше x_to");
            }
            if (request.getCount() == null || request.getCount() <= 0) {
                return ResponseEntity.status(400).body("Количество точек должно быть положительным числом");
            }

            if (!"analytical".equals(function.getType())) {
                return ResponseEntity.status(400).body("Сэмплирование доступно только для аналитических функций");
            }

            List<PointResponse> response = new ArrayList<>();
            double step = (request.getX_to() - request.getX_from()) / (request.getCount() - 1);

            for (int i = 0; i < request.getCount(); i++) {
                double x = request.getX_from() + (i * step);
                double y = calculateAnalyticalFunctionValue(function, x);

                FunctionPoints new_point = new FunctionPoints(function, x, y);
                FunctionPoints saved_point = singleSearchService.saveFunctionPoint(new_point);
                response.add(new PointResponse(generatePointId(saved_point), saved_point.getX_value(), saved_point.getY_value()));            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при сэмплировании аналитической функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/analytical_functions/{function_id}/composition")
    public ResponseEntity<?> getFunctionComposition(@PathVariable("function_id") Long function_id) {
        try {
            logger.info("GET /analytical_functions/{}/composition - получение состава функции", function_id);

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (function_id == null || function_id <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }

            Optional<Functions> function_opt = singleSearchService.findFunctionById(function_id);
            if (function_opt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            Functions function = function_opt.get();
            if (!function.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (!"composite".equals(function.getSource())) {
                return ResponseEntity.status(400).body("Функция не является композитной");
            }

            List<CompositeFunctionElements> composition = multipleSearchService.findCompositeFunctionElementsByCompositeId(function_id);
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

    private Double calculateAnalyticalFunctionValue(Functions analytical_function, double x) {
        try {
            if ("composite".equals(analytical_function.getSource())) {
                return calculateCompositeFunction(analytical_function, x);
            }
            return calculateBasicFunction(analytical_function.getName(), x);
        } catch (Exception e) {
            logger.error("Ошибка вычисления функции '{}' для x={}", analytical_function.getName(), x, e);
            return Double.NaN;
        }
    }

    private Double calculateCompositeFunction(Functions composite_function, double x) {
        try {
            List<CompositeFunctionElements> composition = multipleSearchService.findCompositeFunctionElementsByCompositeId(composite_function.getId());
            composition.sort((a, b) -> a.getFunctionOrder().compareTo(b.getFunctionOrder()));

            if (composition.isEmpty()) {
                return Double.NaN;
            }

            double result = x;
            for (CompositeFunctionElements element : composition) {
                Functions component_function = element.getFunction();
                result = calculateAnalyticalFunctionValue(component_function, result);
                if (Double.isNaN(result)) {
                    break;
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("Ошибка вычисления композитной функции '{}'", composite_function.getName(), e);
            return Double.NaN;
        }
    }

    private Double calculateBasicFunction(String function_name, double x) {
        if (function_name == null) return Double.NaN;

        switch (function_name.toLowerCase()) {
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
                return new IdentityFunction().apply(x);
        }
    }
    private Long generatePointId(FunctionPoints point) {
        if (point.getFunction() == null || point.getX_value() == null) {
            return 0L;
        }
        long functionId = point.getFunction().getId();
        double xValue = point.getX_value();
        long xBits = Double.doubleToLongBits(xValue);
        long id = (functionId << 32) ^ (xBits >>> 32) ^ (xBits & 0xFFFFFFFFL);
        return id & Long.MAX_VALUE;
    }
}