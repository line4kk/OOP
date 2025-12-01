package engine.controller;

import engine.dto.*;
import engine.entity.FunctionPoints;
import engine.entity.Users;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired private SingleSearchService singleSearchService;
    @Autowired private MultipleSearchService multipleSearchService;
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
            logger.info("PUT /users/settings/factory_types - обновление типа фабрики: {}", request.getFactoryType());

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getFactoryType() == null ||
                    (!request.getFactoryType().equals("linked_list") && !request.getFactoryType().equals("array"))) {
                return ResponseEntity.status(400).body("Некорректный тип фабрики");
            }

            current_user.setFactoryType(request.getFactoryType());
            Users updated_user = singleSearchService.saveUser(current_user);

            logger.info("Пользователь {} обновил тип фабрики на: {}",
                    current_user.getUsername(), request.getFactoryType());

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
                return ResponseEntity.ok(response);
            }

            logger.warn("Неудачная попытка аутентификации для пользователя: {}", request.getUsername());
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

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUserByAdmin(@RequestBody UserRegisterRequest request) {
        try {
            logger.info("POST /admin/users - создание пользователя администратором: {}", request.getUsername());

            Users current_admin = securityUtils.getCurrentUser();
            if (current_admin == null || !"admin".equals(current_admin.getRole())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

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

            if (request.getRole() == null ||
                    (!request.getRole().equals("user") && !request.getRole().equals("admin"))) {
                return ResponseEntity.status(400).body("Некорректная роль пользователя");
            }

            if (request.getFactory_type() == null ||
                    (!request.getFactory_type().equals("linked_list") && !request.getFactory_type().equals("array"))) {
                return ResponseEntity.status(400).body("Некорректный тип фабрики");
            }

            if (singleSearchService.findUserByUsername(request.getUsername()) != null) {
                return ResponseEntity.status(409).body("Имя пользователя уже занято");
            }

            String hashed_password = passwordEncoder.encode(request.getPassword());
            Users new_user = new Users(request.getUsername(), hashed_password, request.getRole(), request.getFactory_type());
            Users saved_user = singleSearchService.saveUser(new_user);

            logger.info("Администратор {} создал пользователя: {} с ролью: {}",
                    current_admin.getUsername(), request.getUsername(), request.getRole());

            UserResponse response = mapToUserResponse(saved_user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании пользователя администратором", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/users/{user_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable("user_id") Long user_id) {
        try {
            logger.info("GET /users/{} - получение пользователя по ID", user_id);

            Users currentAdmin = securityUtils.getCurrentUser();
            if (currentAdmin == null || !"admin".equals(currentAdmin.getRole())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Users user = singleSearchService.findUserById(user_id);
            if (user == null) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }

            UserResponse response = mapToUserResponse(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении пользователя по ID", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            logger.info("GET /admin/users - получение списка всех пользователей");

            Users current_admin = securityUtils.getCurrentUser();
            if (current_admin == null || !"admin".equals(current_admin.getRole())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<Users> users = multipleSearchService.findAllUsers();
            List<UserResponse> response = users.stream()
                    .map(this::mapToUserResponse)
                    .collect(java.util.stream.Collectors.toList());

            logger.info("Администратор {} запросил список пользователей, найдено: {}",
                    current_admin.getUsername(), users.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении списка пользователей", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @DeleteMapping("/admin/users/{user_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("user_id") Long user_id) {
        try {
            logger.info("DELETE /admin/users/{} - удаление пользователя", user_id);

            Users current_admin = securityUtils.getCurrentUser();
            if (current_admin == null || !"admin".equals(current_admin.getRole())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (user_id == null || user_id <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID пользователя");
            }

            Users user_to_delete = singleSearchService.findUserById(user_id);
            if (user_to_delete == null) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }

            if (current_admin.getId().equals(user_id)) {
                return ResponseEntity.status(400).body("Невозможно удалить собственный аккаунт");
            }

            singleSearchService.deleteUser(user_id);

            logger.info("Администратор {} удалил пользователя: {}",
                    current_admin.getUsername(), user_to_delete.getUsername());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private UserResponse mapToUserResponse(Users user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getFactoryType());
    }

    public static class FactoryTypeUpdateRequest {
        private String factoryType;

        public String getFactoryType() { return factoryType; }
        public void setFactoryType(String factoryType) { this.factoryType = factoryType; }
    }
}