package entity;

import jakarta.persistence.*;

@Entity
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

    public Operations() {}

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public FunctionPoints getPoint1() { return point1; }
    public void setPoint1(FunctionPoints point1) { this.point1 = point1; }

    public FunctionPoints getPoint2() { return point2; }
    public void setPoint2(FunctionPoints point2) { this.point2 = point2; }

    public Double getResultY() { return resultY; }
    public void setResultY(Double resultY) { this.resultY = resultY; }
}