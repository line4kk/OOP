package engine.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationRequest {
    private Long point1Id;
    private Long point2Id;
    private String operation;
}