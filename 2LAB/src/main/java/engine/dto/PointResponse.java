package engine.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointResponse {
    private Long id;
    private Double x;
    private Double y;
}
