package services;

import dao.FunctionDAO;
import dao.FunctionPointDAO;
import dao.OperationResultPointDAO;
import dao.UserDAO;
import exceptions.DivisionByZeroException;
import functions.TabulatedFunction;
import functions.Point;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import model.Function;
import model.FunctionPoint;
import model.OperationResultPoint;
import model.User;
import model.dto.requests.OperationRequest;
import model.dto.responses.PointResponse;
import operations.TabulatedDifferentialOperator;
import operations.TabulatedFunctionOperationService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

public class OperationService extends AbstractService<FunctionDAO> {
    private final FunctionPointDAO pointDAO = new FunctionPointDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OperationResultPointDAO operationResultPointDAO = new OperationResultPointDAO();
    private static final Set<String> SUPPORTED_OPERATIONS = new HashSet<>();

    static {
        SUPPORTED_OPERATIONS.add("add");
        SUPPORTED_OPERATIONS.add("subtract");
        SUPPORTED_OPERATIONS.add("multiplication");
        SUPPORTED_OPERATIONS.add("division");
        SUPPORTED_OPERATIONS.add("derive");
    }

    public OperationService() {
        super(new FunctionDAO());
    }

    public List<PointResponse> getOperationResult(OperationRequest request) {
        if (request == null || request.getOperation() == null || request.getOperation().isBlank()) {
            throw new IllegalArgumentException("Bad Request");
        }

        String operation = request.getOperation().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_OPERATIONS.contains(operation)) {
            throw new NoSuchElementException("Неизвестная операция");
        }

        Function firstFunction = dao.selectById(request.getFunction1Id());
        if (firstFunction == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        if (operation.equals("derive")) {
            return buildDerivativeResponses(firstFunction);
        }

        Function secondFunction = resolveSecondFunction(request);
        List<FunctionPoint> firstPoints = getPoints(firstFunction.getId());
        List<FunctionPoint> secondPoints = getPoints(secondFunction.getId());
        ensureCompatiblePoints(firstPoints, secondPoints);

        List<PointResponse> responses = new ArrayList<>(firstPoints.size());
        for (int i = 0; i < firstPoints.size(); i++) {
            FunctionPoint firstPoint = firstPoints.get(i);
            FunctionPoint secondPoint = secondPoints.get(i);

            Double cachedValue = isCommutative(operation)
                    ? operationResultPointDAO.getResultYOfCommutativeOperation(operation, firstPoint.getId(), secondPoint.getId())
                    : operationResultPointDAO.getResultYOfNonCommutativeOperation(operation, firstPoint.getId(), secondPoint.getId());

            double resultY;
            if (cachedValue != null) {
                resultY = cachedValue;
            } else {
                resultY = calculate(operation, firstPoint.getYValue(), secondPoint.getYValue());
                operationResultPointDAO.insert(new OperationResultPoint(firstPoint.getId(), secondPoint.getId(), operation, resultY));
            }

            responses.add(new PointResponse(null, firstPoint.getXValue(), resultY));
        }

        return responses;
    }

    private Function resolveSecondFunction(OperationRequest request) {
        Long secondFunctionId = request.getFunction2Id();
        if (secondFunctionId == null) {
            throw new IllegalArgumentException("Bad Request");
        }

        Function secondFunction = dao.selectById(secondFunctionId);
        if (secondFunction == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        return secondFunction;
    }

    private List<FunctionPoint> getPoints(long functionId) {
        List<FunctionPoint> points = pointDAO.selectByFunctionId(functionId);
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Bad Request");
        }
        return points;
    }

    private double calculate(String operation, double firstY, double secondY) {
        return switch (operation) {
            case "add" -> firstY + secondY;
            case "subtract" -> firstY - secondY;
            case "multiplication" -> firstY * secondY;
            case "division" -> {
                if (secondY == 0.0) {
                    throw new DivisionByZeroException("Деление на ноль");
                }
                yield firstY / secondY;
            }
            default -> throw new NoSuchElementException("Неизвестная операция");
        };
    }

    private void ensureCompatiblePoints(List<FunctionPoint> firstPoints, List<FunctionPoint> secondPoints) {
        if (firstPoints.size() != secondPoints.size()) {
            throw new IllegalArgumentException("Bad Request");
        }

        for (int i = 0; i < firstPoints.size(); i++) {
            double x1 = firstPoints.get(i).getXValue();
            double x2 = secondPoints.get(i).getXValue();
            if (Double.compare(x1, x2) != 0) {
                throw new IllegalArgumentException("Bad Request");
            }
        }
    }

    private List<PointResponse> buildDerivativeResponses(Function firstFunction) {
        TabulatedFunctionFactory factory = resolveFactory(firstFunction.getUserId());
        TabulatedFunction tabulatedFunction = toTabulatedFunction(factory, firstFunction.getId());
        TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator(factory);
        TabulatedFunction result = differentialOperator.derive(tabulatedFunction);

        Point[] points = TabulatedFunctionOperationService.asPoints(result);
        List<PointResponse> responses = new ArrayList<>(points.length);
        for (Point point : points) {
            responses.add(new PointResponse(null, point.x, point.y));
        }
        return responses;
    }

    private TabulatedFunctionFactory resolveFactory(long userId) {
        User user = userDAO.select(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь не найден");
        }

        return switch (user.getFactoryType()) {
            case "linked_list" -> new LinkedListTabulatedFunctionFactory();
            case "array" -> new ArrayTabulatedFunctionFactory();
            default -> throw new IllegalArgumentException("Bad Request");
        };
    }

    private TabulatedFunction toTabulatedFunction(TabulatedFunctionFactory factory, long functionId) {
        List<FunctionPoint> points = getPoints(functionId);

        double[] xValues = new double[points.size()];
        double[] yValues = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            xValues[i] = points.get(i).getXValue();
            yValues[i] = points.get(i).getYValue();
        }
        return factory.create(xValues, yValues);
    }

    private boolean isCommutative(String operation) {
        return operation.equals("add") || operation.equals("multiplication");
    }
}