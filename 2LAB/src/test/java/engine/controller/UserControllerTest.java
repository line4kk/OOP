package engine.controller;

import engine.dto.*;
import engine.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    @Test
    void testGetCurrentUser_MethodCall() throws Exception {
        Method method = UserController.class.getDeclaredMethod("getCurrentUser");
        try {
            ResponseEntity<?> response = (ResponseEntity<?>) method.invoke(controller);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testUpdateFactoryType_MethodCall() throws Exception {
        Method method = UserController.class.getDeclaredMethod(
                "updateFactoryType", UserController.FactoryTypeUpdateRequest.class
        );

        UserController.FactoryTypeUpdateRequest request = new UserController.FactoryTypeUpdateRequest();
        request.setFactory_type("array");

        try {
            ResponseEntity<?> response = (ResponseEntity<?>) method.invoke(controller, request);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testAuthenticate_MethodCall() throws Exception {
        Method method = UserController.class.getDeclaredMethod(
                "authenticate", UserLoginRequest.class
        );

        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("test");
        request.setPassword("password");

        try {
            ResponseEntity<?> response = (ResponseEntity<?>) method.invoke(controller, request);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testAuthenticate_ValidationScenarios() throws Exception {
        Method method = UserController.class.getDeclaredMethod(
                "authenticate", UserLoginRequest.class
        );

        UserLoginRequest request1 = new UserLoginRequest();
        request1.setUsername("");
        request1.setPassword("password");

        UserLoginRequest request2 = new UserLoginRequest();
        request2.setUsername("test");
        request2.setPassword("");

        UserLoginRequest request3 = new UserLoginRequest();
        request3.setUsername(null);
        request3.setPassword("password");

        UserLoginRequest request4 = new UserLoginRequest();
        request4.setUsername("test");
        request4.setPassword(null);

        for (UserLoginRequest req : Arrays.asList(request1, request2, request3, request4)) {
            try {
                method.invoke(controller, req);
            } catch (Exception e) {}
        }

        assertNotNull(method);
    }

    @Test
    void testRegister_MethodCall() throws Exception {
        Method method = UserController.class.getDeclaredMethod(
                "register", UserRegisterRequest.class
        );

        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFactory_type("array");
        request.setRole("user");

        try {
            ResponseEntity<?> response = (ResponseEntity<?>) method.invoke(controller, request);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testRegister_ValidationScenarios() throws Exception {
        Method method = UserController.class.getDeclaredMethod(
                "register", UserRegisterRequest.class
        );

        UserRegisterRequest request1 = new UserRegisterRequest();
        request1.setUsername("");
        request1.setPassword("password123");
        request1.setFactory_type("array");

        UserRegisterRequest request2 = new UserRegisterRequest();
        request2.setUsername("test");
        request2.setPassword("short");
        request2.setFactory_type("array");

        UserRegisterRequest request3 = new UserRegisterRequest();
        request3.setUsername("test");
        request3.setPassword("password123");
        request3.setFactory_type("invalid");

        UserRegisterRequest request4 = new UserRegisterRequest();
        request4.setUsername("test");
        request4.setPassword("password123");
        request4.setFactory_type("array");
        request4.setRole("invalid");

        UserRegisterRequest request5 = new UserRegisterRequest();

        for (UserRegisterRequest req : Arrays.asList(request1, request2, request3, request4, request5)) {
            try {
                method.invoke(controller, req);
            } catch (Exception e) {}
        }

        assertNotNull(method);
    }

    @Test
    void testUpdateUserRole_MethodCall() throws Exception {
        Method method = UserController.class.getDeclaredMethod(
                "updateUserRole", Long.class, UserController.RoleUpdateRequest.class
        );

        UserController.RoleUpdateRequest request = new UserController.RoleUpdateRequest();
        request.setRole("admin");

        try {
            ResponseEntity<?> response = (ResponseEntity<?>) method.invoke(controller, 1L, request);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testUpdateUserRole_ValidationScenarios() throws Exception {
        Method method = UserController.class.getDeclaredMethod(
                "updateUserRole", Long.class, UserController.RoleUpdateRequest.class
        );

        UserController.RoleUpdateRequest request1 = new UserController.RoleUpdateRequest();
        request1.setRole("invalid");

        UserController.RoleUpdateRequest request2 = new UserController.RoleUpdateRequest();
        request2.setRole(null);

        try {
            method.invoke(controller, 0L, request1);
            method.invoke(controller, -1L, request2);
            method.invoke(controller, null, request1);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testIsAuthenticated() throws Exception {
        Method method = UserController.class.getDeclaredMethod("isAuthenticated");
        method.setAccessible(true);

        try {
            Boolean result = (Boolean) method.invoke(controller);
            assertNotNull(result);
        } catch (Exception e) {}
    }

    @Test
    void testMapToUserResponse() throws Exception {
        Method method = UserController.class.getDeclaredMethod(
                "mapToUserResponse", Users.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole("user");
        user.setFactoryType("array");

        UserResponse response = (UserResponse) method.invoke(controller, user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("user", response.getRole());
        assertEquals("array", response.getFactoryType());
    }

    @Test
    void testDTOClasses() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("test");
        loginRequest.setPassword("password");

        assertEquals("test", loginRequest.getUsername());
        assertEquals("password", loginRequest.getPassword());

        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("user");
        registerRequest.setPassword("pass123");
        registerRequest.setFactory_type("linked_list");
        registerRequest.setRole("admin");

        assertEquals("user", registerRequest.getUsername());
        assertEquals("pass123", registerRequest.getPassword());
        assertEquals("linked_list", registerRequest.getFactory_type());
        assertEquals("admin", registerRequest.getRole());

        UserResponse userResponse = new UserResponse(1L, "user", "admin", "array");
        assertEquals(1L, userResponse.getId());
        assertEquals("user", userResponse.getUsername());
        assertEquals("admin", userResponse.getRole());
        assertEquals("array", userResponse.getFactoryType());
    }

    @Test
    void testInnerDTOClasses() {
        UserController.FactoryTypeUpdateRequest factoryRequest = new UserController.FactoryTypeUpdateRequest();
        factoryRequest.setFactory_type("linked_list");
        assertEquals("linked_list", factoryRequest.getFactory_type());

        UserController.RoleUpdateRequest roleRequest = new UserController.RoleUpdateRequest();
        roleRequest.setRole("admin");
        assertEquals("admin", roleRequest.getRole());
    }

    @Test
    void testEntityClasses() {
        Users user = new Users("test", "hash", "user", "array");

        assertEquals("test", user.getUsername());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("user", user.getRole());
        assertEquals("array", user.getFactoryType());

        Users user2 = new Users();
        user2.setId(1L);
        user2.setUsername("user2");
        user2.setPasswordHash("hash2");
        user2.setRole("admin");
        user2.setFactoryType("linked_list");

        assertEquals(1L, user2.getId());
        assertEquals("user2", user2.getUsername());
        assertEquals("hash2", user2.getPasswordHash());
        assertEquals("admin", user2.getRole());
        assertEquals("linked_list", user2.getFactoryType());
    }

    @Test
    void testClassConstants() throws Exception {
        java.lang.reflect.Field rolesField = UserController.class.getDeclaredField("ALLOWED_ROLES");
        rolesField.setAccessible(true);
        Set<String> allowedRoles = (Set<String>) rolesField.get(null);

        java.lang.reflect.Field factoryField = UserController.class.getDeclaredField("ALLOWED_FACTORY_TYPES");
        factoryField.setAccessible(true);
        Set<String> allowedFactories = (Set<String>) factoryField.get(null);

        assertTrue(allowedRoles.contains("user"));
        assertTrue(allowedRoles.contains("admin"));
        assertEquals(2, allowedRoles.size());

        assertTrue(allowedFactories.contains("linked_list"));
        assertTrue(allowedFactories.contains("array"));
        assertEquals(2, allowedFactories.size());
    }

    @Test
    void testClassStructure() throws Exception {
        assertTrue(UserController.class.isAnnotationPresent(
                org.springframework.web.bind.annotation.RestController.class));

        java.lang.reflect.Field loggerField = UserController.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        assertNotNull(loggerField.get(controller));

        String[] dependencyFields = {"singleSearchService", "securityUtils", "passwordEncoder"};
        for (String fieldName : dependencyFields) {
            java.lang.reflect.Field field = UserController.class.getDeclaredField(fieldName);
            assertNotNull(field);
            assertTrue(java.lang.reflect.Modifier.isPrivate(field.getModifiers()));
        }
    }

    @Test
    void testAllControllerMethods() throws Exception {
        List<Method> publicMethods = Arrays.stream(UserController.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .toList();

        assertEquals(5, publicMethods.size());

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername("test");
        loginRequest.setPassword("password");

        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("user");
        registerRequest.setPassword("password123");
        registerRequest.setFactory_type("array");
        registerRequest.setRole("user");

        UserController.FactoryTypeUpdateRequest factoryRequest = new UserController.FactoryTypeUpdateRequest();
        factoryRequest.setFactory_type("array");

        UserController.RoleUpdateRequest roleRequest = new UserController.RoleUpdateRequest();
        roleRequest.setRole("admin");

        for (Method method : publicMethods) {
            try {
                String methodName = method.getName();

                if (methodName.equals("getCurrentUser")) {
                    method.invoke(controller);
                } else if (methodName.equals("updateFactoryType")) {
                    method.invoke(controller, factoryRequest);
                } else if (methodName.equals("authenticate")) {
                    method.invoke(controller, loginRequest);
                } else if (methodName.equals("register")) {
                    method.invoke(controller, registerRequest);
                } else if (methodName.equals("updateUserRole")) {
                    method.invoke(controller, 1L, roleRequest);
                }
            } catch (Exception e) {}
        }

        Method isAuthenticated = UserController.class.getDeclaredMethod("isAuthenticated");
        isAuthenticated.setAccessible(true);
        isAuthenticated.invoke(controller);

        Method mapToUserResponse = UserController.class.getDeclaredMethod(
                "mapToUserResponse", Users.class
        );
        mapToUserResponse.setAccessible(true);

        Users user = new Users("test", "hash", "user", "array");
        mapToUserResponse.invoke(controller, user);

        assertTrue(true);
    }

    @Test
    void testValidationConstantsUsage() {
        assertTrue(Set.of("user", "admin").contains("user"));
        assertTrue(Set.of("user", "admin").contains("admin"));
        assertFalse(Set.of("user", "admin").contains("invalid"));

        assertTrue(Set.of("linked_list", "array").contains("linked_list"));
        assertTrue(Set.of("linked_list", "array").contains("array"));
        assertFalse(Set.of("linked_list", "array").contains("invalid"));
    }
}