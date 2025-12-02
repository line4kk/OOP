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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            if (!isValidOperation(request.getOperation())) {
                return ResponseEntity.status(404).body("Неизвестная операция");
            }
            if (request.getFunction1_id() == null || request.getFunction1_id() <= 0) {
                return ResponseEntity.status(400).body("Некорректные входные данные: function1_id");
            }

            Optional<Functions> function1Opt = singleSearchService.findFunctionById(request.getFunction1_id());
            if (function1Opt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция с ID " + request.getFunction1_id() + " не найдена");
            }
            Functions function1 = function1Opt.get();
            if (!securityUtils.canAccessUserData(function1.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            if (!isTabulated(function1)) {
                return ResponseEntity.status(400).body("Операции поддерживаются только для табулированных функций");
            }

            List<FunctionPoints> function1Points = multipleSearchService.findPointsByFunction(request.getFunction1_id());
            if (function1Points.isEmpty()) {
                return ResponseEntity.status(404).body("Функция с ID " + request.getFunction1_id() + " не имеет точек");
            }
            TabulatedFunction tabulatedFunction1 = createTabulatedFunction(function1Points);

            List<PointResponse> response;
            switch (request.getOperation()) {
                case "derive":
                    Functions derivativeFunction = createDerivativeFunction(function1, tabulatedFunction1);
                    response = multipleSearchService.findPointsByFunction(derivativeFunction.getId()).stream()
                            .map(p -> new PointResponse(p.getId(), p.getX_value(), p.getY_value()))
                            .collect(Collectors.toList());
                    break;
                case "add":
                case "subtract":
                case "multiplication":
                case "division":
                    if (request.getFunction2_id() == null || request.getFunction2_id() <= 0) {
                        return ResponseEntity.status(400).body("Некорректные входные данные: function2_id требуется для операции " + request.getOperation());
                    }

                    Optional<Functions> function2Opt = singleSearchService.findFunctionById(request.getFunction2_id());
                    if (function2Opt.isEmpty()) {
                        return ResponseEntity.status(404).body("Функция с ID " + request.getFunction2_id() + " не найдена");
                    }
                    Functions function2 = function2Opt.get();
                    if (!securityUtils.canAccessUserData(function2.getUser())) {
                        return ResponseEntity.status(403).body("Forbidden");
                    }
                    if (!isTabulated(function2)) {
                        return ResponseEntity.status(400).body("Операции поддерживаются только для табулированных функций");
                    }

                    List<FunctionPoints> function2Points = multipleSearchService.findPointsByFunction(request.getFunction2_id());
                    if (function2Points.isEmpty()) {
                        return ResponseEntity.status(404).body("Функция с ID " + request.getFunction2_id() + " не имеет точек");
                    }
                    if ("division".equals(request.getOperation()) && hasZeroY(function2Points)) {
                        return ResponseEntity.status(418).body("I'm a teapot (division by zero)");
                    }

                    TabulatedFunction tabulatedFunction2 = createTabulatedFunction(function2Points);
                    Functions resultFunction = createBinaryOperationFunction(function1, function2, request.getOperation(), tabulatedFunction1, tabulatedFunction2);
                    response = multipleSearchService.findPointsByFunction(resultFunction.getId()).stream()
                            .map(p -> new PointResponse(p.getId(), p.getX_value(), p.getY_value()))
                            .collect(Collectors.toList());
                    break;
                default:
                    return ResponseEntity.status(404).body("Неизвестная операция");
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

    private Functions createDerivativeFunction(Functions sourceFunction, TabulatedFunction function) {
        TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
        TabulatedFunction derivativeFunction = differentialOperator.derive(function);

        Functions newFunction = new Functions();
        newFunction.setUser(sourceFunction.getUser());
        newFunction.setName(sourceFunction.getName() + "_derivative");
        newFunction.setType("array_tabulated");
        newFunction.setSource("operation");

        Functions savedFunction = singleSearchService.saveFunction(newFunction);
        saveFunctionPoints(savedFunction, derivativeFunction);

        return savedFunction;
    }

    private Functions createBinaryOperationFunction(Functions func1, Functions func2, String operation,
                                                    TabulatedFunction tabFunc1, TabulatedFunction tabFunc2) {
        TabulatedFunctionOperationService operationService = new TabulatedFunctionOperationService();
        TabulatedFunction resultFunction;

        switch (operation) {
            case "add":
                resultFunction = operationService.add(tabFunc1, tabFunc2);
                break;
            case "subtract":
                resultFunction = operationService.subtract(tabFunc1, tabFunc2);
                break;
            case "multiplication":
                resultFunction = operationService.multiplication(tabFunc1, tabFunc2);
                break;
            case "division":
                resultFunction = operationService.division(tabFunc1, tabFunc2);
                break;
            default:
                throw new IllegalStateException("Не удалось выполнить операцию: " + operation);
        }

        Functions newFunction = new Functions();
        newFunction.setUser(func1.getUser());
        newFunction.setName(func1.getName() + "_" + operation + "_" + func2.getName());
        newFunction.setType("array_tabulated");
        newFunction.setSource("operation");

        Functions savedFunction = singleSearchService.saveFunction(newFunction);
        saveFunctionPoints(savedFunction, resultFunction);

        return savedFunction;
    }

    private void saveFunctionPoints(Functions function, TabulatedFunction tabulatedFunction) {
        for (int i = 0; i < tabulatedFunction.getCount(); i++) {
            double x = tabulatedFunction.getX(i);
            double y = tabulatedFunction.getY(i);
            FunctionPoints functionPoint = new FunctionPoints(function, x, y);
            singleSearchService.saveFunctionPoint(functionPoint);
        }
    }

    private TabulatedFunction createTabulatedFunction(List<FunctionPoints> points) {
        List<FunctionPoints> sortedPoints = points.stream()
                .sorted(Comparator.comparingDouble(FunctionPoints::getX_value))
                .collect(Collectors.toList());

        double[] xValues = new double[sortedPoints.size()];
        double[] yValues = new double[sortedPoints.size()];

        for (int i = 0; i < sortedPoints.size(); i++) {
            FunctionPoints point = sortedPoints.get(i);
            xValues[i] = point.getX_value();
            yValues[i] = point.getY_value();
        }

        return new ArrayTabulatedFunctionFactory().create(xValues, yValues);
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

    private boolean isTabulated(Functions function) {
        return "array_tabulated".equals(function.getType()) || "linked_list_tabulated".equals(function.getType());
    }

    private boolean hasZeroY(List<FunctionPoints> points) {
        return points.stream().anyMatch(p -> p.getY_value() == 0.0);
    }
}