package engine.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class FunctionSamplingRequest {

    @JsonProperty("function_id")
    private Long function_id;

    @JsonProperty("analytical_function_id")
    private Long analytical_function_id;

    @JsonProperty("x_from")
    private Double x_from;

    @JsonProperty("x_to")
    private Double x_to;

    private Integer count;
}