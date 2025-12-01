package engine.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class OperationRequest {
    @JsonProperty("function1_id")
    private Long function1_id;

    @JsonProperty("function2_id")
    private Long function2_id;

    private String operation;
}