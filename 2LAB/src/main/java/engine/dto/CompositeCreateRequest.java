package engine.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeCreateRequest {
    private List<Long> functionIdsInOrder;
    private String name;
}
