package engine.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionCreateRequest {
    private String name;
    private String type;
    private String source;
}
