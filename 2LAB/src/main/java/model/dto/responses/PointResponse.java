package model.dto.responses;

import model.FunctionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PointResponse {
    private static final Logger logger = LoggerFactory.getLogger(PointResponse.class);

    private Long id;
    private double x;
    private double y;

    public PointResponse() {}

    public PointResponse(Long id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public static PointResponse from(FunctionPoint point) {
        logger.info("Создан PointResponse из FunctionPoint: id={}, x={}, y={}", point.getId(), point.getXValue(), point.getYValue());
        return new PointResponse(point.getId(), point.getXValue(), point.getYValue());
    }

    public static List<PointResponse> fromList(List<FunctionPoint> points) {
        return points.stream()
                .map(PointResponse::from)
                .toList();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    @Override
    public String toString() {
        return "PointResponse{id=" + id + ", x=" + x + ", y=" + y + "}";
    }
}