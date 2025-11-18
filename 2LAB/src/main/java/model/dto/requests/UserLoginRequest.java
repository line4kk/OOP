package model.dto.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLoginRequest {
    private static final Logger logger = LoggerFactory.getLogger(UserLoginRequest.class);
    private String username;
    private String password;

    public UserLoginRequest() {
        logger.info("Создан UserLoginRequest");
    }

    public UserLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
        logger.info("Создан UserLoginRequest: username={}", username);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "UserLoginRequest{username='" + username + "'}";
    }
}