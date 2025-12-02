package engine.service;

import engine.entity.Users;
import engine.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameReturnsSpringSecurityUser() {
        Users entity = new Users();
        entity.setUsername("alice");
        entity.setPasswordHash("secret");
        entity.setRole("user");

        when(usersRepository.findByUsername("alice")).thenReturn(entity);

        UserDetails details = customUserDetailsService.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsernameThrowsWhenMissing() {
        when(usersRepository.findByUsername("missing"))
                .thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing"));
    }

    @Test
    void loadUserEntityByUsernameReturnsEntity() {
        Users entity = new Users();
        entity.setUsername("bob");
        entity.setPasswordHash("hash");
        when(usersRepository.findByUsername("bob")).thenReturn(entity);

        Users result = customUserDetailsService.loadUserEntityByUsername("bob");

        assertSame(entity, result);
    }

    @Test
    void loadUserEntityByUsernameThrowsWhenMissing() {
        when(usersRepository.findByUsername("ghost")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserEntityByUsername("ghost"));
    }
}