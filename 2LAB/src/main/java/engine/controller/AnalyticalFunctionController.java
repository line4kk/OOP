package engine.controller;

import engine.dto.*;
import engine.entity.CompositeFunctionElements;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import engine.repository.CompositeFunctionElementsRepository;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
import functions.IdentityFunction;
import functions.SqrFunction;
import functions.UnitFunction;
import functions.ZeroFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@PreAuthorize("hasAnyRole('USER','ADMIN')")
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

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<Long> components = request.getFunctionIdsInOrder();
            if (components == null || components.size() < 2) {
                return ResponseEntity.status(400).body("Ошибка. Некорректные данные: требуется минимум 2 функции");
            }
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя функции не может быть пустым");
            }

            boolean nameExists = multipleSearchService.findFunctionsByName(request.getName()).stream()
                    .anyMatch(f -> securityUtils.canAccessUserData(f.getUser()));
            if (nameExists) {
                return ResponseEntity.status(409).body("Композиция с таким именем уже существует");
            }

            Set<Long> seenComponents = new HashSet<>();
            List<Functions> orderedComponents = new ArrayList<>();
            for (Long componentId : components) {
                if (componentId == null || componentId <= 0 || !seenComponents.add(componentId)) {
                    return ResponseEntity.status(400).body("Некорректный ID функции в списке");
                }
                Optional<Functions> componentOpt = singleSearchService.findFunctionById(componentId);
                if (componentOpt.isEmpty()) {
                    return ResponseEntity.status(404).body("Функция не найдена");
                }
                Functions component = componentOpt.get();
                if (!securityUtils.canAccessUserData(component.getUser())) {
                    return ResponseEntity.status(403).body("Forbidden");
                }
                orderedComponents.add(component);
            }

            Functions savedFunction = singleSearchService.saveFunction(
                    new Functions(currentUser, request.getName(), "analytical", "composite")
            );

            for (int order = 0; order < orderedComponents.size(); order++) {
                compositeFunctionElementsRepository.save(
                        new CompositeFunctionElements(savedFunction, order, orderedComponents.get(order))
                );
            }

            return ResponseEntity.ok(new FunctionResponse(
                    savedFunction.getId(), savedFunction.getName(), savedFunction.getType(), savedFunction.getSource()
            ));
        } catch (Exception e) {
            logger.error("Ошибка при создании композитной функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/functions/{function_id}/composition")
    public ResponseEntity<?> getCompositeFunctionElements(@PathVariable("function_id") Long functionId) {
        try {
            logger.info("GET /functions/{}/composition - получение состава композитной функции", functionId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> compositeFunctionOpt = singleSearchService.findFunctionById(functionId);
            if (compositeFunctionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            Functions compositeFunction = compositeFunctionOpt.get();
            if (!securityUtils.canAccessUserData(compositeFunction.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            if (!"composite".equals(compositeFunction.getSource()) ||
                    !"analytical".equals(compositeFunction.getType())) {
                return ResponseEntity.status(400).body("Функция не является композитной аналитической");
            }

            List<CompositeElementResponse> response = multipleSearchService
                    .findCompositeFunctionElementsOrdered(functionId)
                    .stream()
                    .map(element -> new CompositeElementResponse(
                            element.getId(),
                            element.getFunctionOrder(),
                            element.getFunction().getId()
                    ))
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении состава композитной функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/functions/{function_id}/sampling")
    @Transactional
    public ResponseEntity<?> addFunctionPointsBySampling(@PathVariable("function_id") Long functionId,
                                                         @RequestBody FunctionSamplingRequest request) {
        try {
            logger.info("POST /functions/{}/sampling - сэмплирование аналитической функции", functionId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (functionId == null || functionId <= 0 || request.getFunction_id() == null ||
                    !functionId.equals(request.getFunction_id())) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }
            if (request.getAnalytical_function_id() == null) {
                return ResponseEntity.status(400).body("Некорректные данные: analytical_function_id обязателен");
            }
            if (request.getX_from() == null || request.getX_to() == null) {
                return ResponseEntity.status(400).body("Некорректные данные: x_from и x_to обязательны");
            }
            if (request.getX_from() >= request.getX_to()) {
                return ResponseEntity.status(400).body("x_from должен быть меньше x_to");
            }
            if (request.getCount() == null || request.getCount() < 2) {
                return ResponseEntity.status(400).body("Количество точек должно быть положительным числом");
            }

            Optional<Functions> targetFunctionOpt = singleSearchService.findFunctionById(functionId);
            if (targetFunctionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions targetFunction = targetFunctionOpt.get();
            if (!securityUtils.canAccessUserData(targetFunction.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            if (!"linked_list_tabulated".equals(targetFunction.getType()) &&
                    !"array_tabulated".equals(targetFunction.getType())) {
                return ResponseEntity.status(400).body("Сэмплирование доступно только для табулированных функций");
            }

            Optional<Functions> analyticalFunctionOpt = singleSearchService.findFunctionById(request.getAnalytical_function_id());
            if (analyticalFunctionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions analyticalFunction = analyticalFunctionOpt.get();
            if (!securityUtils.canAccessUserData(analyticalFunction.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            if (!"analytical".equals(analyticalFunction.getType())) {
                return ResponseEntity.status(400).body("Сэмплирование доступно только для аналитических функций");
            }

            List<PointResponse> response = new ArrayList<>();
            double step = (request.getX_to() - request.getX_from()) / (request.getCount() - 1);
            Set<Double> generatedXValues = new HashSet<>();

            for (int i = 0; i < request.getCount(); i++) {
                double x = request.getX_from() + (i * step);
                if (!generatedXValues.add(x) || singleSearchService.findFunctionPointByX(functionId, x).isPresent()) {
                    return ResponseEntity.status(409).body("Добавление существующей точки");
                }

                double y = calculateAnalyticalFunctionValue(analyticalFunction, x);
                FunctionPoints savedPoint = singleSearchService.saveFunctionPoint(new FunctionPoints(targetFunction, x, y));
                response.add(new PointResponse(savedPoint.getId(), savedPoint.getX_value(), savedPoint.getY_value()));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при сэмплировании аналитической функции", e);
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
            List<CompositeFunctionElements> composition = multipleSearchService.findCompositeFunctionElementsByCompositeId(compositeFunction.getId());
            composition.sort((a, b) -> a.getFunctionOrder().compareTo(b.getFunctionOrder()));

            if (composition.isEmpty()) {
                return Double.NaN;
            }

            double result = x;
            for (CompositeFunctionElements element : composition) {
                Functions componentFunction = element.getFunction();
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
                return new IdentityFunction().apply(x);
        }
    }
}