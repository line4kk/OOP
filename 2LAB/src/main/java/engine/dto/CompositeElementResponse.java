package engine.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeElementResponse {
    private Long id;
    private Integer order;

    @JsonProperty("function_id")
    private Long function_id;
}