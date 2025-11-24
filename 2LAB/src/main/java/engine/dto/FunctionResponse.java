package engine.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionResponse {
    private Long id;
    private String name;
    private String type;
    private String source;
}
