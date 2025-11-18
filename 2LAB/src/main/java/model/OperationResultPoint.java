package model;

public class OperationResultPoint {
    private long id;
    private long point1Id;
    private long point2Id;
    private String operation;
    private double resultY;

    public OperationResultPoint(long id, long point1Id, long point2Id, String operation, double resultY) {
        this.id = id;
        this.point1Id = point1Id;
        this.point2Id = point2Id;
        this.operation = operation;
        this.resultY = resultY;
    }

    public OperationResultPoint(long point1Id, long point2Id, String operation, double resultY) {
        this.point1Id = point1Id;
        this.point2Id = point2Id;
        this.operation = operation;
        this.resultY = resultY;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public long getPoint1Id() {
        return point1Id;
    }

    public void setPoint1Id(long point1Id) {
        this.point1Id = point1Id;
    }

    public long getPoint2Id() {
        return point2Id;
    }

    public void setPoint2Id(long point2Id) {
        this.point2Id = point2Id;
    }

    public double getResultY() {
        return resultY;
    }

    public void setResultY(double resultY) {
        this.resultY = resultY;
    }

    @Override
    public String toString() {
        return "OperationResultPoint{" +
                "id=" + id +
                ", operation='" + operation + '\'' +
                ", point1Id=" + point1Id +
                ", point2Id=" + point2Id +
                ", resultY=" + resultY +
                '}';
    }
}
