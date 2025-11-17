package entity;

import jakarta.persistence.*;
import jakarta.persistence.IdClass;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "point1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Operations> operationsAsPoint1 = new ArrayList<>();

    @OneToMany(mappedBy = "point2", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Operations> operationsAsPoint2 = new ArrayList<>();

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
    public void setYValue(Double yValue) {
        if (this.yValue != null && !this.yValue.equals(yValue)) {
            this.operationsAsPoint1.clear();
            this.operationsAsPoint2.clear();
        }
        this.yValue = yValue;
    }

    public List<Operations> getOperationsAsPoint1() { return operationsAsPoint1; }
    public List<Operations> getOperationsAsPoint2() { return operationsAsPoint2; }

}