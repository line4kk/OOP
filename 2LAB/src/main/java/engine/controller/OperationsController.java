package engine.controller;

import engine.dto.OperationRequest;
import engine.dto.PointResponse;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
import operations.TabulatedDifferentialOperator;
import operations.TabulatedFunctionOperationService;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class OperationsController {

    private static final Logger logger = LoggerFactory.getLogger(OperationsController.class);

    @Autowired private MultipleSearchService multipleSearchService;
    @Autowired private SingleSearchService singleSearchService;
    @Autowired private SecurityUtils securityUtils;

    @PostMapping("/operation")
    @Transactional
    public ResponseEntity<?> getOperationResult(@RequestBody OperationRequest request) {
        try {
            logger.info("POST /operation - операция: {} между функциями {} и {}",
                    request.getOperation(), request.getFunction1_id(), request.getFunction2_id());

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getOperation() == null || !isValidOperation(request.getOperation())) {
                return ResponseEntity.status(404).body("Неизвестная операция");
            }

            if (request.getFunction1_id() == null || request.getFunction1_id() <= 0) {
                return ResponseEntity.status(400).body("Некорректные входные данные: function1_id");
            }

            Optional<Functions> function1_opt = singleSearchService.findFunctionById(request.getFunction1_id());
            if (function1_opt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция с ID " + request.getFunction1_id() + " не найдена");
            }

            Functions function1 = function1_opt.get();
            if (!function1.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<FunctionPoints> function1_points = multipleSearchService.findPointsByFunction(request.getFunction1_id());
            if (function1_points.isEmpty()) {
                return ResponseEntity.status(404).body("Функция с ID " + request.getFunction1_id() + " не имеет точек");
            }

            TabulatedFunction tabulated_function1 = createTabulatedFunction(function1_points);

            List<PointResponse> response = null;

            switch (request.getOperation()) {
                case "derive":
                    Functions derivative_function = createDerivativeFunction(function1, tabulated_function1);
                    List<FunctionPoints> derivative_points = multipleSearchService.findPointsByFunction(derivative_function.getId());
                    response = derivative_points.stream()
                            .map(point -> new PointResponse(generatePointId(point), point.getX_value(), point.getY_value()))
                            .collect(Collectors.toList());
                    break;

                case "add":
                case "subtract":
                case "multiplication":
                case "division":
                    if (request.getFunction2_id() == null || request.getFunction2_id() <= 0) {
                        return ResponseEntity.status(400).body("Некорректные входные данные: function2_id требуется для операции " + request.getOperation());
                    }

                    Optional<Functions> function2_opt = singleSearchService.findFunctionById(request.getFunction2_id());
                    if (function2_opt.isEmpty()) {
                        return ResponseEntity.status(404).body("Функция с ID " + request.getFunction2_id() + " не найдена");
                    }

                    Functions function2 = function2_opt.get();
                    if (!function2.getUser().getId().equals(current_user.getId())) {
                        return ResponseEntity.status(403).body("Forbidden");
                    }

                    List<FunctionPoints> function2_points = multipleSearchService.findPointsByFunction(request.getFunction2_id());
                    if (function2_points.isEmpty()) {
                        return ResponseEntity.status(404).body("Функция с ID " + request.getFunction2_id() + " не имеет точек");
                    }

                    TabulatedFunction tabulated_function2 = createTabulatedFunction(function2_points);
                    Functions result_function = createBinaryOperationFunction(function1, function2, request.getOperation(), tabulated_function1, tabulated_function2);
                    List<FunctionPoints> result_points = multipleSearchService.findPointsByFunction(result_function.getId());
                    response = result_points.stream()
                            .map(point -> new PointResponse(generatePointId(point), point.getX_value(), point.getY_value()))
                            .collect(Collectors.toList());
                    break;

                default:
                    return ResponseEntity.status(404).body("Неизвестная операция");
            }

            if (response == null) {
                return ResponseEntity.status(500).body("Ошибка при выполнении операции: не удалось создать результат");
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при выполнении операции", e);
            return ResponseEntity.status(400).body("Некорректные входные данные: " + e.getMessage());
        } catch (ArithmeticException e) {
            logger.error("Арифметическая ошибка при выполнении операции", e);
            if (request.getOperation().equals("division")) {
                return ResponseEntity.status(418).body("I'm a teapot (division by zero)");
            }
            return ResponseEntity.status(400).body("Арифметическая ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка при выполнении операции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private Functions createDerivativeFunction(Functions source_function, TabulatedFunction function) {
        TabulatedDifferentialOperator differential_operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative_function = differential_operator.derive(function);

        Functions new_function = new Functions();
        new_function.setUser(source_function.getUser());
        new_function.setName(source_function.getName() + "_derivative");
        new_function.setType("analytical");
        new_function.setSource("operation");

        Functions saved_function = singleSearchService.saveFunction(new_function);
        saveFunctionPoints(saved_function, derivative_function);

        return saved_function;
    }

    private Functions createBinaryOperationFunction(Functions func1, Functions func2, String operation,
                                                    TabulatedFunction tab_func1, TabulatedFunction tab_func2) {
        TabulatedFunctionOperationService operation_service = new TabulatedFunctionOperationService();
        TabulatedFunction result_function = null;

        switch (operation) {
            case "add":
                result_function = operation_service.add(tab_func1, tab_func2);
                break;
            case "subtract":
                result_function = operation_service.subtract(tab_func1, tab_func2);
                break;
            case "multiplication":
                result_function = operation_service.multiplication(tab_func1, tab_func2);
                break;
            case "division":
                result_function = operation_service.division(tab_func1, tab_func2);
                break;
        }

        if (result_function == null) {
            throw new IllegalStateException("Не удалось выполнить операцию: " + operation);
        }

        Functions new_function = new Functions();
        new_function.setUser(func1.getUser());
        new_function.setName(func1.getName() + "_" + operation + "_" + func2.getName());
        new_function.setType("analytical");
        new_function.setSource("operation");

        Functions saved_function = singleSearchService.saveFunction(new_function);
        saveFunctionPoints(saved_function, result_function);

        return saved_function;
    }

    private void saveFunctionPoints(Functions function, TabulatedFunction tabulated_function) {
        for (int i = 0; i < tabulated_function.getCount(); i++) {
            double x = tabulated_function.getX(i);
            double y = tabulated_function.getY(i);

            FunctionPoints function_point = new FunctionPoints(function, x, y);
            singleSearchService.saveFunctionPoint(function_point);
        }
    }

    private TabulatedFunction createTabulatedFunction(List<FunctionPoints> points) {
        List<FunctionPoints> sorted_points = points.stream()
                .sorted(Comparator.comparingDouble(FunctionPoints::getX_value))
                .collect(Collectors.toList());

        double[] x_values = new double[sorted_points.size()];
        double[] y_values = new double[sorted_points.size()];

        for (int i = 0; i < sorted_points.size(); i++) {
            FunctionPoints point = sorted_points.get(i);
            x_values[i] = point.getX_value();
            y_values[i] = point.getY_value();
        }

        return new ArrayTabulatedFunctionFactory().create(x_values, y_values);
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

    private boolean isValidOperation(String operation) {
        return operation != null && (
                operation.equals("add") ||
                        operation.equals("subtract") ||
                        operation.equals("multiplication") ||
                        operation.equals("division") ||
                        operation.equals("derive")
        );
    }
}