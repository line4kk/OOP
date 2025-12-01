package engine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "function_points", uniqueConstraints = {
        @UniqueConstraint(name = "uk_function_point_x", columnNames = {"function_id", "x_value"})
})
public class FunctionPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private Functions function;

    @Column(name = "x_value", nullable = false)
    private Double x_value;

    @Column(name = "y_value", nullable = false)
    private Double y_value;

    @OneToMany(mappedBy = "point1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Operations> operationsAsPoint1 = new ArrayList<>();

    @OneToMany(mappedBy = "point2", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Operations> operationsAsPoint2 = new ArrayList<>();

    public FunctionPoints(Functions function, Double xValue, Double yValue) {
        this.function = function;
        this.x_value = xValue;
        this.y_value = yValue;
    }

    public void setYValue(Double yValue) {
        if (this.y_value != null && !this.y_value.equals(yValue)) {
            this.operationsAsPoint1.clear();
            this.operationsAsPoint2.clear();
        }
        this.y_value = yValue;
    }

}