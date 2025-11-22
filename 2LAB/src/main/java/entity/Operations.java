package entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "operations_result_points")
public class Operations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation", nullable = false, length = 50)
    private String operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "point1_function_id", referencedColumnName = "function_id"),
            @JoinColumn(name = "point1_x_value", referencedColumnName = "x_value")
    })
    private FunctionPoints point1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "point2_function_id", referencedColumnName = "function_id"),
            @JoinColumn(name = "point2_x_value", referencedColumnName = "x_value")
    })
    private FunctionPoints point2;

    @Column(name = "result_y", nullable = false)
    private Double resultY;

    public Operations(String operation, FunctionPoints point1, Double resultY) {
        this.operation = operation;
        this.point1 = point1;
        this.resultY = resultY;
    }

    public Operations(String operation, FunctionPoints point1, FunctionPoints point2, Double resultY) {
        this.operation = operation;
        this.point1 = point1;
        this.point2 = point2;
        this.resultY = resultY;
    }
}