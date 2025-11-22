package entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    public FunctionPoints(Functions function, Double xValue, Double yValue) {
        this.function = function;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public void setYValue(Double yValue) {
        if (this.yValue != null && !this.yValue.equals(yValue)) {
            this.operationsAsPoint1.clear();
            this.operationsAsPoint2.clear();
        }
        this.yValue = yValue;
    }

}