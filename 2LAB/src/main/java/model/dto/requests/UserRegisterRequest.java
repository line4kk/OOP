package model.dto.requests;

import dao.FunctionDAO;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRegisterRequest {
    private static final Logger logger = LoggerFactory.getLogger(UserRegisterRequest.class);
    private String username;
    private String password;
    private String role;
    private String factoryType;

    public UserRegisterRequest() {
        logger.info("Создан UserRegisterRequest");
    }

    public UserRegisterRequest(String username, String password, String role, String factoryType) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.factoryType = factoryType;
        logger.info("Создан UserRegisterRequest: username={}", username);
    }


    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFactoryType() { return factoryType; }
    public void setFactoryType(String factoryType) { this.factoryType = factoryType; }

    public User toEntity() {
        logger.info("Преобразование UserRegisterRequest → User: username={}", this.getUsername());
        User user = new User(
                this.getUsername(),
                this.getPassword(),        // потом захешируется в сервисе
                this.getRole(),
                this.getFactoryType()
        );
        return user;
    }
    @Override
    public String toString() {
        return "UserRegisterRequest{username='" + username + "', role='" + role + "'}";
    }
}