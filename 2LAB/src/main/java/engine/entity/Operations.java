package engine.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
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
    @JoinColumn(name = "point1_id")
    private FunctionPoints point1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point2_id")
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