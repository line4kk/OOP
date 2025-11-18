package model.dto.responses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserResponse {
    private static final Logger logger = LoggerFactory.getLogger(UserResponse.class);
    private Long id;
    private String username;
    private String role;
    private String factoryType;

    public UserResponse() {}

    public UserResponse(Long id, String username, String role, String factoryType) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.factoryType = factoryType;
    }

    public static UserResponse from(model.User user) {
        logger.info("Создан UserResponse из User: id={}, username={}", user.getId(), user.getUsername());
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getFactoryType()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFactoryType() { return factoryType; }
    public void setFactoryType(String factoryType) { this.factoryType = factoryType; }

    @Override
    public String toString() {
        return "UserResponse{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}