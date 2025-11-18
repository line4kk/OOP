package model.dto.responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationResultResponse {
    private static final Logger logger = LoggerFactory.getLogger(OperationResultResponse.class);

    private double resultY;

    public OperationResultResponse() {}

    public OperationResultResponse(double resultY) {
        this.resultY = resultY;
    }

    public static OperationResultResponse from(double resultY) {
        logger.info("Создан OperationResultResponse: resultY={}", resultY);
        return new OperationResultResponse(resultY);
    }

    public double getResultY() {
        return resultY;
    }

    public void setResultY(double resultY) {
        this.resultY = resultY;
    }

    @Override
    public String toString() {
        return "OperationResultResponse{resultY=" + resultY + "}";
    }
}