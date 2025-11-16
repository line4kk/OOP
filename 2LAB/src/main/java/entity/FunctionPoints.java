package entity;

import javax.persistence.*;
import javax.persistence.IdClass;

@Entity
@Table(name = "function_points")
@IdClass(FunctionPointsId.class)
public class FunctionPoints {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private Functions function;

    @Id
    @Column(name = "x_value", nullable = false)
    private Double xValue;

    @Column(name = "y_value", nullable = false)
    private Double yValue;

    public FunctionPoints() {}

    public FunctionPoints(Functions function, Double xValue, Double yValue) {
        this.function = function;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public Functions getFunction() { return function; }
    public void setFunction(Functions function) { this.function = function; }

    public Double getXValue() { return xValue; }
    public void setXValue(Double xValue) { this.xValue = xValue; }

    public Double getYValue() { return yValue; }
    public void setYValue(Double yValue) { this.yValue = yValue; }

}