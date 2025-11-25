package services;

import dao.FunctionDAO;
import dao.FunctionPointDAO;
import exceptions.AlreadyExistsException;
import model.Function;
import model.FunctionPoint;
import model.dto.requests.CompositeCreateRequest;
import model.dto.requests.FunctionCreateRequest;
import model.dto.requests.OperationRequest;
import model.dto.requests.PointRequest;
import model.dto.responses.FunctionResponse;
import model.dto.responses.OperationResultResponse;
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

    public FunctionResponse getFunction(long functionId) {
        Function function = dao.selectById(functionId);
        if (function != null) {
            return FunctionResponse.from(function);
        }
        throw new NoSuchElementException("Функция не найдена");
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

    public List<PointResponse> addFunctionPointsSampling(long anFunId, double xFrom, double xTo, int count) {
        return new ArrayList<PointResponse>();
        // ЗАГЛУШКА
    }

    public FunctionResponse createCompositeFunction(CompositeCreateRequest compositeCreateRequest) {
        return new FunctionResponse();
        // ЗАГЛУШКА
    }

    public OperationResultResponse getOperationResult(OperationRequest operationRequest) {
        return new OperationResultResponse();
        // ЗАГЛУШКА
    }
}
