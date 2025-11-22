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

    public CompositeFunctionElements(Functions composite, Integer functionOrder, Functions function) {
        this.composite = composite;
        this.functionOrder = functionOrder;
        this.function = function;
    }

}
