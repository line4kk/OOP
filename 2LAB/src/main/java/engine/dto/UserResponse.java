package engine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String role;

    @JsonProperty("factory_type")
    private String factoryType;

    @JsonProperty("factory_type")
    public String getFactoryType() {
        return factoryType;
    }

    @JsonProperty("factory_type")
    public void setFactoryType(String factoryType) {
        this.factoryType = factoryType;
    }

}