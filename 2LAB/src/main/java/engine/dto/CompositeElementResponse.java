package engine.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeElementResponse {
    private Long id;
    private Integer order;
    private Long functionId;
}