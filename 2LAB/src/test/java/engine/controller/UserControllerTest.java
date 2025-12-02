package engine.controller;

import engine.dto.UserLoginRequest;
import engine.dto.UserRegisterRequest;
import engine.entity.Users;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private SingleSearchService singleSearchService;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController controller;

    private Users user;

    @BeforeEach
    void setUp() {
        user = new Users("u", "hash", "user", "array");
        user.setId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserChecksAuthenticationAndPresence() {
        SecurityContextHolder.clearContext();
        ResponseEntity<?> forbidden = controller.getCurrentUser();
        assertEquals(403, forbidden.getStatusCodeValue());

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("u", "p", "ROLE_USER"));
        when(securityUtils.getCurrentUser()).thenReturn(null);
        ResponseEntity<?> notFound = controller.getCurrentUser();
        assertEquals(404, notFound.getStatusCodeValue());

        when(securityUtils.getCurrentUser()).thenReturn(user);
        ResponseEntity<?> ok = controller.getCurrentUser();
        assertEquals(200, ok.getStatusCodeValue());
        assertTrue(ok.getBody().toString().contains("u"));
    }

    @Test
    void updateFactoryTypeValidatesAuthenticationAndPayload() {
        UserController.FactoryTypeUpdateRequest request = new UserController.FactoryTypeUpdateRequest();
        request.setFactory_type("invalid");

        SecurityContextHolder.clearContext();
        ResponseEntity<?> forbidden = controller.updateFactoryType(request);
        assertEquals(403, forbidden.getStatusCodeValue());

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("u", "p", "ROLE_USER"));
        when(securityUtils.getCurrentUser()).thenReturn(null);
        ResponseEntity<?> notFound = controller.updateFactoryType(request);
        assertEquals(404, notFound.getStatusCodeValue());

        when(securityUtils.getCurrentUser()).thenReturn(user);
        ResponseEntity<?> badType = controller.updateFactoryType(request);
        assertEquals(400, badType.getStatusCodeValue());

        request.setFactory_type("linked_list");
        when(singleSearchService.saveUser(user)).thenReturn(user);
        ResponseEntity<?> ok = controller.updateFactoryType(request);
        assertEquals(200, ok.getStatusCodeValue());
        verify(singleSearchService).saveUser(user);
    }

    @Test
    void authenticateValidatesCredentialsAndPassword() {
        UserLoginRequest request = new UserLoginRequest();
        ResponseEntity<?> emptyUser = controller.authenticate(request);
        assertEquals(400, emptyUser.getStatusCodeValue());

        request.setUsername("u");
        ResponseEntity<?> emptyPassword = controller.authenticate(request);
        assertEquals(400, emptyPassword.getStatusCodeValue());

        request.setPassword("pass");
        when(singleSearchService.findUserByUsername("u")).thenReturn(null);
        ResponseEntity<?> notFound = controller.authenticate(request);
        assertEquals(401, notFound.getStatusCodeValue());

        when(singleSearchService.findUserByUsername("u")).thenReturn(user);
        when(passwordEncoder.matches("pass", "hash")).thenReturn(false);
        ResponseEntity<?> wrongPassword = controller.authenticate(request);
        assertEquals(401, wrongPassword.getStatusCodeValue());

        when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        ResponseEntity<?> ok = controller.authenticate(request);
        assertEquals(200, ok.getStatusCodeValue());
    }

    @Test
    void registerValidatesFieldsAndRoles() {
        UserRegisterRequest request = new UserRegisterRequest();
        ResponseEntity<?> missingUser = controller.register(request);
        assertEquals(400, missingUser.getStatusCodeValue());

        request.setUsername("u");
        request.setPassword("short");
        request.setFactory_type("array");
        ResponseEntity<?> badPassword = controller.register(request);
        assertEquals(400, badPassword.getStatusCodeValue());

        request.setPassword("longenough");
        request.setFactory_type("bad");
        ResponseEntity<?> badFactory = controller.register(request);
        assertEquals(400, badFactory.getStatusCodeValue());

        request.setFactory_type("array");
        request.setRole("invalid");
        when(securityUtils.getCurrentUser()).thenReturn(user);
        ResponseEntity<?> badRole = controller.register(request);
        assertEquals(400, badRole.getStatusCodeValue());

        request.setRole("admin");
        when(securityUtils.isAdmin()).thenReturn(false);
        ResponseEntity<?> forbiddenRole = controller.register(request);
        assertEquals(403, forbiddenRole.getStatusCodeValue());

        when(securityUtils.isAdmin()).thenReturn(true);
        when(singleSearchService.findUserByUsername("u")).thenReturn(user);
        ResponseEntity<?> duplicate = controller.register(request);
        assertEquals(409, duplicate.getStatusCodeValue());
    }

    @Test
    void registerCreatesUserWhenValid() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("newuser");
        request.setPassword("goodpassword");
        request.setFactory_type("array");
        request.setRole("user");

        when(securityUtils.getCurrentUser()).thenReturn(null);
        when(securityUtils.isAdmin()).thenReturn(false);
        when(singleSearchService.findUserByUsername("newuser")).thenReturn(null);
        when(passwordEncoder.encode("goodpassword")).thenReturn("hashed");
        when(singleSearchService.saveUser(any())).thenAnswer(invocation -> {
            Users saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        ResponseEntity<?> response = controller.register(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(singleSearchService).saveUser(any());
    }

    @Test
    void updateUserRoleValidatesInputAndPersists() {
        UserController.RoleUpdateRequest request = new UserController.RoleUpdateRequest();
        request.setRole("invalid");

        ResponseEntity<?> badId = controller.updateUserRole(0L, request);
        assertEquals(400, badId.getStatusCodeValue());

        ResponseEntity<?> badRole = controller.updateUserRole(1L, request);
        assertEquals(400, badRole.getStatusCodeValue());

        request.setRole("admin");
        when(singleSearchService.findUserById(1L)).thenReturn(null);
        ResponseEntity<?> notFound = controller.updateUserRole(1L, request);
        assertEquals(404, notFound.getStatusCodeValue());

        when(singleSearchService.findUserById(1L)).thenReturn(user);
        when(singleSearchService.saveUser(user)).thenReturn(user);
        ResponseEntity<?> ok = controller.updateUserRole(1L, request);
        assertEquals(200, ok.getStatusCodeValue());
        verify(singleSearchService).saveUser(user);
    }
}