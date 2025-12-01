package engine.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    private String username;
    private String password;
    private String role;

    @JsonProperty("factory_type")
    private String factory_type;
}
