package model.dto.requests;

import model.OperationResultPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationRequest {
    private static final Logger logger = LoggerFactory.getLogger(OperationRequest.class);
    private long point1Id;
    private long point2Id;
    private String operation;

    public OperationRequest() {
        logger.info("Создан OperationRequest");
    }

    public OperationRequest(long point1Id, long point2Id, String operation) {
        logger.info("Создан {}", toString());
        this.point1Id = point1Id;
        this.point2Id = point2Id;
        this.operation = operation;
    }

    public long getPoint1Id() { return point1Id; }
    public void setPoint1Id(long point1Id) { this.point1Id = point1Id; }
    public long getPoint2Id() { return point2Id; }
    public void setPoint2Id(long point2Id) { this.point2Id = point2Id; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public OperationResultPoint toEntity(double resultY) {
        logger.info("Преобразование OperationRequest -> OperationResultPoint: {} {} {} -> {}",
                getPoint1Id(), getOperation(), getPoint2Id(), resultY);
        OperationResultPoint result = new OperationResultPoint(
                getPoint1Id(),
                getPoint2Id(),
                getOperation(),
                resultY
        );
        return result;
    }

    @Override
    public String toString() {
        return "OperationRequest{point1Id=" + point1Id + ", point2Id=" + point2Id + ", operation='" + operation + "'}";
    }
}