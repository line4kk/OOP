package entity;

import javax.persistence.*;

@Entity
@Table(name = "composite_function_elements") // Исправлено имя таблицы
public class CompositeFunctionElements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composite_id", nullable = false)
    private Functions composite;

    @Column(name = "function_order", nullable = false)
    private Integer functionOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private Functions function;

    public CompositeFunctionElements() {}

    public CompositeFunctionElements(Functions composite, Integer functionOrder, Functions function) {
        this.composite = composite;
        this.functionOrder = functionOrder;
        this.function = function;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Functions getComposite() { return composite; }
    public void setComposite(Functions composite) { this.composite = composite; }

    public Integer getFunctionOrder() { return functionOrder; }
    public void setFunctionOrder(Integer functionOrder) { this.functionOrder = functionOrder; }

    public Functions getFunction() { return function; }
    public void setFunction(Functions function) { this.function = function; }
}
