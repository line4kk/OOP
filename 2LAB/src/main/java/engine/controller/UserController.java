package engine.controller;

import engine.dto.UserLoginRequest;
import engine.dto.UserRegisterRequest;
import engine.dto.UserResponse;
import engine.entity.Users;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired private SingleSearchService singleSearchService;
    @Autowired private SecurityUtils securityUtils;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<?> getCurrentUser() {
        try {
            logger.info("GET /users - получение информации о текущем пользователе");

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                logger.warn("Попытка доступа без аутентификации");
                return ResponseEntity.status(403).body("Forbidden");
            }

            UserResponse response = mapToUserResponse(current_user);
            logger.debug("Возвращены данные пользователя: {}", current_user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении пользователя", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PutMapping("/users/settings/factory_types")
    public ResponseEntity<?> updateFactoryType(@RequestBody FactoryTypeUpdateRequest request) {
        try {
            logger.info("PUT /users/settings/factory_types - обновление типа фабрики: {}", request.getFactory_type());

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getFactory_type() == null ||
                    (!request.getFactory_type().equals("linked_list") && !request.getFactory_type().equals("array"))) {
                return ResponseEntity.status(400).body("Некорректный тип фабрики");
            }

            current_user.setFactoryType(request.getFactory_type());
            Users updated_user = singleSearchService.saveUser(current_user);

            logger.info("Пользователь {} обновил тип фабрики на: {}",
                    current_user.getUsername(), request.getFactory_type());

            UserResponse response = mapToUserResponse(updated_user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении типа фабрики", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/users/auth")
    public ResponseEntity<?> authenticate(@RequestBody UserLoginRequest request) {
        try {
            logger.info("POST /users/auth - аутентификация пользователя: {}", request.getUsername());

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя пользователя не может быть пустым");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Пароль не может быть пустым");
            }

            Users user = singleSearchService.findUserByUsername(request.getUsername());
            if (user != null && passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                logger.info("Успешная аутентификация пользователя: {}", request.getUsername());
                UserResponse response = mapToUserResponse(user);
            }
            if (request.getFactory_type() == null ||
                    (!request.getFactory_type().equals("linked_list") && !request.getFactory_type().equals("array"))) {
                return ResponseEntity.status(400).body("Некорректный тип фабрики");
            }

            if (singleSearchService.findUserByUsername(request.getUsername()) != null) {
                logger.warn("Попытка регистрации с существующим именем: {}", request.getUsername());
                return ResponseEntity.status(409).body("Имя пользователя уже занято");
            }

            String hashed_password = passwordEncoder.encode(request.getPassword());
            Users new_user = new Users(request.getUsername(), hashed_password, request.getRole(), request.getFactory_type());
            Users saved_user = singleSearchService.saveUser(new_user);

            logger.info("Успешная регистрация пользователя: {}", request.getUsername());

            UserResponse response = mapToUserResponse(saved_user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при регистрации", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private UserResponse mapToUserResponse(Users user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getFactoryType());
    }

    public static class FactoryTypeUpdateRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("factory_type")
        private String factory_type;

        public String getFactory_type() { return factory_type; }
        public void setFactory_type(String factory_type) { this.factory_type = factory_type; }
    }
}

