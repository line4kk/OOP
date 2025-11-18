package model.dto.requests;

import model.FunctionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointRequest {
    private static final Logger logger = LoggerFactory.getLogger(PointRequest.class);
    private double x;
    private double y;

    public PointRequest() {
        logger.info("Создан PointRequest");
    }

    public PointRequest(double x, double y) {
        this.x = x;
        this.y = y;
        logger.info("Создан PointRequest: ({}, {})", x, y);
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public FunctionPoint toEntity(long functionId) {
        logger.info("Преобразование PointRequest → FunctionPoint для functionId={}", functionId);
        return new FunctionPoint(functionId, this.getX(), this.getY());
    }

    @Override
    public String toString() {
        return "PointRequest{x=" + x + ", y=" + y + "}";
    }
}