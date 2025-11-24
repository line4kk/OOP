package engine.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionsResponse {
    private List<FunctionResponse> functions;
}
