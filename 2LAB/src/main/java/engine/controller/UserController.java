package engine.controller;

import engine.dto.*;
import lombok.Data;
import engine.entity.Users;
import engine.service.SingleSearchService;
import engine.service.MultipleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired private SingleSearchService singleSearchService;
    @Autowired private MultipleSearchService multipleSearchService;

    @GetMapping("/users")
    public ResponseEntity<?> getCurrentUser() {
        try {
            logger.info("GET /users - получение информации о текущем пользователе");
            List<Users> users = multipleSearchService.findAllUsers();
            if (users.isEmpty()) {
                return ResponseEntity.status(404).body("Информация о пользователе не найдена");
            }
            UserResponse response = mapToUserResponse(users.get(0));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении пользователя", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }
    @PatchMapping("/users/settings/factory_types")
    public ResponseEntity<?> updateFactoryType(@RequestBody FactoryTypeUpdateRequest request) {
        try {
            logger.info("PATCH /users/settings/factory_types - обновление типа фабрики: {}", request.getFactoryType());

            if (request.getFactoryType() == null ||
                    (!request.getFactoryType().equals("linked_list") && !request.getFactoryType().equals("array"))) {
                return ResponseEntity.status(400).body("Некорректный тип фабрики");
            }

            UserResponse response = new UserResponse(1L, "currentUser", "user", request.getFactoryType());
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
            if (user != null) {
                UserResponse response = mapToUserResponse(user);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(401).body("Неправильный логин или пароль");
        } catch (Exception e) {
            logger.error("Ошибка при аутентификации", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            logger.info("POST /users/register - регистрация пользователя: {}", request.getUsername());

            // Валидация имени пользователя
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя пользователя не может быть пустым");
            }
            if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
                return ResponseEntity.status(400).body("Имя пользователя должно быть от 3 до 50 символов");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Пароль не может быть пустым");
            }
            if (request.getPassword().length() < 8) {
                return ResponseEntity.status(400).body("Пароль должен содержать минимум 8 символов");
            }
            if (request.getPassword().length() > 64) {
                return ResponseEntity.status(400).body("Пароль не может превышать 64 символа");
            }

            if (request.getRole() == null ||
                    (!request.getRole().equals("user") && !request.getRole().equals("admin"))) {
                return ResponseEntity.status(400).body("Некорректная роль пользователя");
            }

            if (request.getFactoryType() == null ||
                    (!request.getFactoryType().equals("linked_list") && !request.getFactoryType().equals("array"))) {
                return ResponseEntity.status(400).body("Некорректный тип фабрики");
            }

            if (singleSearchService.findUserByUsername(request.getUsername()) != null) {
                return ResponseEntity.status(409).body("Имя пользователя уже занято");
            }

            UserResponse response = new UserResponse(1L, request.getUsername(), request.getRole(), request.getFactoryType());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при регистрации", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private UserResponse mapToUserResponse(Users user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getFactoryType());
    }

    @Data
    public static class FactoryTypeUpdateRequest {
        private String factoryType;
    }
}