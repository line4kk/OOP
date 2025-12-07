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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final Set<String> ALLOWED_ROLES = Set.of("user", "admin");
    private static final Set<String> ALLOWED_FACTORY_TYPES = Set.of("linked_list", "array");

    @Autowired private SingleSearchService singleSearchService;
    @Autowired private SecurityUtils securityUtils;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        try {
            logger.info("GET /users - получение информации о текущем пользователе");

            if (!isAuthenticated()) {
                logger.warn("Попытка доступа без аутентификации");
                return ResponseEntity.status(403).body("Forbidden");
            }

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                logger.warn("Информация о пользователе не найдена в хранилище");
                return ResponseEntity.status(404).body("Информация о пользователе не найдена");
            }

            UserResponse response = mapToUserResponse(currentUser);
            logger.debug("Возвращены данные пользователя: {}", currentUser.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении пользователя", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PutMapping("/users/settings/factory_types")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateFactoryType(@RequestBody FactoryTypeUpdateRequest request) {
        try {
            logger.info("PUT /users/settings/factory_types - обновление типа фабрики: {}", request.getFactory_type());

            if (!isAuthenticated()) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(404).body("Информация о пользователе не найдена");
            }

            String factoryType = request.getFactory_type();
            if (factoryType == null || !ALLOWED_FACTORY_TYPES.contains(factoryType)) {
                return ResponseEntity.status(400).body("Некорректный тип фабрики");
            }

            currentUser.setFactoryType(factoryType);
            Users updatedUser = singleSearchService.saveUser(currentUser);

            logger.info("Пользователь {} обновил тип фабрики на: {}", currentUser.getUsername(), factoryType);

            UserResponse response = mapToUserResponse(updatedUser);
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
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                logger.warn("Неудачная попытка аутентификации пользователя: {}", request.getUsername());
                return ResponseEntity.status(401).body("Неправильный логин или пароль");
            }

            logger.info("Успешная аутентификация пользователя: {}", request.getUsername());
            UserResponse response = mapToUserResponse(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при аутентификации", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            logger.info("POST /users/register - регистрация пользователя: {}", request.getUsername());

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя пользователя не может быть пустым");
            }
            if (request.getPassword() == null || request.getPassword().length() < 8 || request.getPassword().length() > 64) {
                return ResponseEntity.status(400).body("Ошибка. Некорректные данные");
            }
            if (request.getFactory_type() == null || !ALLOWED_FACTORY_TYPES.contains(request.getFactory_type())) {
                return ResponseEntity.status(400).body("Ошибка. Некорректные данные");
            }

            if (singleSearchService.findUserByUsername(request.getUsername()) != null) {
                logger.warn("Попытка регистрации с существующим именем: {}", request.getUsername());
                return ResponseEntity.status(409).body("Имя пользователя уже занято");
            }

            Users currentUser = securityUtils.getCurrentUser();

            String requestedRole = request.getRole() == null ? "user" : request.getRole().toLowerCase();
            if (!ALLOWED_ROLES.contains(requestedRole)) {
                return ResponseEntity.status(400).body("Ошибка. Некорректные данные");
            }
            if ("admin".equals(requestedRole)) {
                boolean haveAdmin = securityUtils.isAdmin();
                if (!haveAdmin) {
                    long adminCount = singleSearchService.countUsersByRole("admin");
                    if (adminCount > 0) {
                        logger.warn("Попытка назначить роль ADMIN без прав: {}", request.getUsername());
                        return ResponseEntity.status(403).body("Назначение роли доступно только администратору");
                    }
                    logger.info("Создание первого администратора в системе: {}", request.getUsername());
                }
            }
            if (currentUser == null && "user".equals(requestedRole)) {
                logger.info("Регистрация без аутентификации: роль установлена по умолчанию USER");
            }

            String hashedPassword = passwordEncoder.encode(request.getPassword());
            Users newUser = new Users(
                    request.getUsername(),
                    hashedPassword,
                    requestedRole,
                    request.getFactory_type()
            );
            Users savedUser = singleSearchService.saveUser(newUser);

            logger.info("Успешная регистрация пользователя: {}", request.getUsername());

            UserResponse response = mapToUserResponse(savedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при регистрации", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PutMapping("/users/{user_id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable("user_id") Long userId,
                                            @RequestBody RoleUpdateRequest request) {
        try {
            logger.info("PUT /users/{}/role - изменение роли пользователя", userId);

            if (userId == null || userId <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID пользователя");
            }
            if (request.getRole() == null || !ALLOWED_ROLES.contains(request.getRole())) {
                return ResponseEntity.status(400).body("Некорректная роль");
            }

            Users targetUser = singleSearchService.findUserById(userId);
            if (targetUser == null) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }

            targetUser.setRole(request.getRole());
            Users savedUser = singleSearchService.saveUser(targetUser);

            logger.info("Роль пользователя {} изменена на {}", savedUser.getUsername(), savedUser.getRole());
            return ResponseEntity.ok(mapToUserResponse(savedUser));
        } catch (Exception e) {
            logger.error("Ошибка при изменении роли пользователя", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
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
    public static class RoleUpdateRequest {
        private String role;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}

