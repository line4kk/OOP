package model;

public class FunctionPoint {
    private long id;
    private long functionId;
    private double xValue;
    private double yValue;

    public FunctionPoint(long id, long functionId, double xValue, double yValue) {
        this.id = id;
        this.functionId = functionId;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public FunctionPoint(long functionId, double xValue, double yValue) {
        this.functionId = functionId;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(long functionId) {
        this.functionId = functionId;
    }

    public double getXValue() {
        return xValue;
    }

    public void setXValue(double xValue) {
        this.xValue = xValue;
    }

    public double getYValue() {
        return yValue;
    }

    public void setYValue(double yValue) {
        this.yValue = yValue;
    }

    @Override
    public String toString() {
        return "FunctionPoint{" +
                "id=" + id +
                ", functionId=" + functionId +
                ", xValue=" + xValue +
                ", yValue=" + yValue +
                '}';
    }
}
