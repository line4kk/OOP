package engine.controller;

import engine.dto.OperationRequest;
import engine.dto.OperationResultResponse;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import engine.repository.FunctionPointsRepository;
import engine.repository.FunctionsRepository;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import operations.TabulatedDifferentialOperator;
import operations.TabulatedFunctionOperationService;
import functions.TabulatedFunction;
import functions.Point;
import functions.factory.ArrayTabulatedFunctionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@RestController
public class OperationsController {

    private static final Logger logger = LoggerFactory.getLogger(OperationsController.class);

    @Autowired private MultipleSearchService multipleSearchService;
    @Autowired private SingleSearchService singleSearchService;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;

    @PostMapping("/operations/results")
    @Transactional
    public ResponseEntity<?> getOperationResult(@RequestBody OperationRequest request) {
        try {
            logger.info("POST /operations/results - операция: {} между функциями точек {} и {}",
                    request.getOperation(), request.getPoint1Id(), request.getPoint2Id());

            if (request.getOperation() == null || !isValidOperation(request.getOperation())) {
                return ResponseEntity.status(404).body("Неизвестная операция");
            }

            if (request.getPoint1Id() == null || request.getPoint1Id() <= 0) {
                return ResponseEntity.status(400).body("Некорректные входные данные: function1_id");
            }

            Functions sourceFunction1 = singleSearchService.findFunctionById(request.getPoint1Id())
                    .orElseThrow(() -> new IllegalArgumentException("Функция с ID " + request.getPoint1Id() + " не найдена"));

            List<FunctionPoints> function1Points = multipleSearchService.findPointsByFunction(request.getPoint1Id());
            if (function1Points.isEmpty()) {
                return ResponseEntity.status(404).body("Функция с ID " + request.getPoint1Id() + " не имеет точек");
            }

            TabulatedFunction function1 = createTabulatedFunction(function1Points);

            Functions resultFunction = null;

            switch (request.getOperation()) {
                case "derive":
                    resultFunction = createDerivativeFunction(sourceFunction1, function1);
                    break;

                case "add":
                case "subtract":
                case "multiplication":
                case "division":
                    if (request.getPoint2Id() == null || request.getPoint2Id() <= 0) {
                        return ResponseEntity.status(400).body("Некорректные входные данные: function2_id требуется для операции " + request.getOperation());
                    }

                    Functions sourceFunction2 = singleSearchService.findFunctionById(request.getPoint2Id())
                            .orElseThrow(() -> new IllegalArgumentException("Функция с ID " + request.getPoint2Id() + " не найдена"));

                    List<FunctionPoints> function2Points = multipleSearchService.findPointsByFunction(request.getPoint2Id());
                    if (function2Points.isEmpty()) {
                        return ResponseEntity.status(404).body("Функция с ID " + request.getPoint2Id() + " не имеет точек");
                    }

                    TabulatedFunction function2 = createTabulatedFunction(function2Points);

                    resultFunction = createBinaryOperationFunction(sourceFunction1, sourceFunction2, request.getOperation(), function1, function2);
                    break;

                default:
                    return ResponseEntity.status(404).body("Неизвестная операция");
            }

            if (resultFunction == null) {
                return ResponseEntity.status(500).body("Ошибка при выполнении операции: не удалось создать результирующую функцию");
            }

            OperationResultResponse response = new OperationResultResponse();
            response.setResultY((double) resultFunction.getId()); // Используем resultY для передачи ID функции
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
        newFunction.setType("derivative");
        newFunction.setSource("operation");

        Functions savedFunction = functionsRepository.save(newFunction);

        saveFunctionPoints(savedFunction, derivativeFunction);

        return savedFunction;
    }

    private Functions createBinaryOperationFunction(Functions func1, Functions func2, String operation,
                                                    TabulatedFunction tabFunc1, TabulatedFunction tabFunc2) {
        TabulatedFunctionOperationService operationService = new TabulatedFunctionOperationService();
        TabulatedFunction resultFunction = null;

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
        }

        if (resultFunction == null) {
            throw new IllegalStateException("Не удалось выполнить операцию: " + operation);
        }

        Functions newFunction = new Functions();
        newFunction.setUser(func1.getUser());
        newFunction.setName(func1.getName() + "_" + operation + "_" + func2.getName());
        newFunction.setType("operation_result");
        newFunction.setSource("operation");

        Functions savedFunction = functionsRepository.save(newFunction);

        saveFunctionPoints(savedFunction, resultFunction);

        return savedFunction;
    }

    private void saveFunctionPoints(Functions function, TabulatedFunction tabulatedFunction) {
        for (int i = 0; i < tabulatedFunction.getCount(); i++) {
            double x = tabulatedFunction.getX(i);
            double y = tabulatedFunction.getY(i);

            FunctionPoints functionPoint = new FunctionPoints(function, x, y);

            functionPointsRepository.save(functionPoint);
        }
    }

    private TabulatedFunction createTabulatedFunction(List<FunctionPoints> points) {
        List<FunctionPoints> sortedPoints = points.stream()
                .sorted(Comparator.comparingDouble(FunctionPoints::getXValue))
                .toList();

        double[] xValues = new double[sortedPoints.size()];
        double[] yValues = new double[sortedPoints.size()];

        for (int i = 0; i < sortedPoints.size(); i++) {
            FunctionPoints point = sortedPoints.get(i);
            xValues[i] = point.getXValue();
            yValues[i] = point.getYValue();
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
}