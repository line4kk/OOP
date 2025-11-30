package services;

import dao.FunctionDAO;
import dao.FunctionPointDAO;
import model.Function;
import model.FunctionPoint;
import model.dto.requests.PointRequest;
import model.dto.responses.PointResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class FunctionPointsService extends AbstractService<FunctionPointDAO> {
    private final FunctionDAO functionsDAO = new FunctionDAO();
    public FunctionPointsService() {
        super(new FunctionPointDAO());
    }

    public List<PointResponse> getFunctionPoints(long functionId) {
        if (functionsDAO.selectById(functionId) == null) {
            throw new NoSuchElementException("Функция не найдена");
        }
        List<FunctionPoint> functionPoints = dao.selectByFunctionId(functionId);

        return PointResponse.fromList(functionPoints);
    }

    public List<PointResponse> addFunctionPoints(long functionId, List<PointRequest> pointRequests) {
        List<FunctionPoint> points = new ArrayList<>();
        for (PointRequest pr : pointRequests) {
            points.add(pr.toEntity(functionId));
        }
        dao.insertList(points);
        // Ошибки обрабатываются прям ↑↑↑ там. (прошу прощения, я устал)
        return PointResponse.fromList(points);
    }

    public void deleteAllFunctionPoints(long userId, long functionId) {
        logger.info("Запрос на удаление всех точек функции id={} от пользователя userId={}", functionId, userId);

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            logger.info("Попытка удалить точки несуществующей функции id={}", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        dao.deleteByFunctionId(functionId);

        logger.info("Успешно удалены все точки функции id={} (пользователь {})", functionId, userId);
    }

    public PointResponse updateFunctionPointY(long userId, long functionId, long pointId, PointRequest pointRequest) {
        logger.info("Обновление Y-значения точки id={} функции id={} пользователем {}", pointId, functionId, userId);

        if (pointRequest.getY() == null) {
            logger.warn("Попытка обновить точку без указания Y: {}", pointRequest);
            throw new IllegalArgumentException("Ошибка. Проверьте, что у каждой точки есть значения x и y");
        }

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            logger.info("Функция id={} не найдена при обновлении точки", functionId);
            throw new NoSuchElementException("Функция не найдена");
        }

        FunctionPoint point = dao.selectById(pointId);
        if (point == null) {
            logger.info("Точка с id={} не найдена", pointId);
            throw new NoSuchElementException("Точка не найдена");
        }

        dao.updateYValue(functionId, point.getXValue(), pointRequest.getY());
        point.setYValue(pointRequest.getY());

        logger.info("Y-значение точки id={} успешно обновлено: y = {}", pointId, pointRequest.getY());
        return PointResponse.from(point);
    }

    public void deleteFunctionPoint(long userId, long functionId, long pointId) {
        logger.info("Удаление точки id={} из функции id={} пользователем {}", pointId, functionId, userId);

        Function function = functionsDAO.selectById(functionId);
        if (function == null) {
            throw new NoSuchElementException("Функция не найдена");
        }

        FunctionPoint point = dao.selectById(pointId);
        if (point == null) {
            logger.info("Попытка удалить несуществующую точку id={}", pointId);
            throw new NoSuchElementException("Точка не найдена");
        }

        dao.deleteById(pointId);

        logger.info("Точка id={} успешно удалена из функции id={}", pointId, functionId);
    }
}
