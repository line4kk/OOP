package engine.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FunctionPointsId implements Serializable {
    private Long function;
    private Double xValue;

}