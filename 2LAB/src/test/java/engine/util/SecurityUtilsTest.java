package engine.util;

import engine.entity.Users;
import engine.repository.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock private UsersRepository usersRepository;
    @InjectMocks private SecurityUtils securityUtils;

    private Users user;

    @BeforeEach
    void setUp() {
        user = new Users(1L, "demo", "hash", "user", "array", List.of());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserReturnsNullWhenNotAuthenticated() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(null, null, List.of());
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Users result = securityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    @Test
    void getCurrentUserReturnsUserWhenAuthenticated() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("demo", "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(usersRepository.findByUsername("demo")).thenReturn(user);

        Users result = securityUtils.getCurrentUser();

        assertThat(result).isEqualTo(user);
    }

    @Test
    void hasRoleChecksAuthority() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("demo", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(securityUtils.hasRole("admin")).isTrue();
        assertThat(securityUtils.hasRole("user")).isFalse();
        assertThat(securityUtils.isAdmin()).isTrue();
    }

    @Test
    void canAccessUserDataReturnsTrueForOwnerOrAdmin() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("demo", "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(usersRepository.findByUsername("demo")).thenReturn(user);

        Users owner = new Users(1L, "demo", "hash", "user", "array", List.of());
        Users admin = new Users(2L, "other", "hash", "admin", "array", List.of());

        assertThat(securityUtils.canAccessUserData(owner)).isTrue();

        Authentication adminAuth = new UsernamePasswordAuthenticationToken("adminUser", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(adminAuth);
        when(usersRepository.findByUsername("adminUser")).thenReturn(admin);

        assertThat(securityUtils.canAccessUserData(owner)).isTrue();
    }
}
