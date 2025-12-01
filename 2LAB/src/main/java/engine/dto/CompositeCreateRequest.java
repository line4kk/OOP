package engine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeCreateRequest {

    @JsonProperty("function_ids_in_order")
    private List<Long> functionIdsInOrder;

    private String name;
}
