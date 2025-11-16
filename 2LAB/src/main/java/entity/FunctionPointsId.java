package entity;

import java.io.Serializable;
import java.util.Objects;

public class FunctionPointsId implements Serializable {
    private Long function;
    private Double xValue;

    public FunctionPointsId() {}

    public FunctionPointsId(Long function, Double xValue) {
        this.function = function;
        this.xValue = xValue;
    }

    public Long getFunction() { return function; }
    public void setFunction(Long function) { this.function = function; }

    public Double getXValue() { return xValue; }
    public void setXValue(Double xValue) { this.xValue = xValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionPointsId that = (FunctionPointsId) o;

        return (function == that.function || (function != null && function.equals(that.function))) &&
                (xValue == that.xValue || (xValue != null && xValue.equals(that.xValue)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, xValue);
    }

}