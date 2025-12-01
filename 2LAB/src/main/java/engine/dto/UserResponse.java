package engine.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String role;

    @JsonProperty("factory_type")
    private String factory_type;
}