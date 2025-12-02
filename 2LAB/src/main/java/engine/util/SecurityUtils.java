package engine.util;

import engine.entity.Users;
import engine.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    private static UsersRepository usersRepository;

    @Autowired
    public SecurityUtils(UsersRepository usersRepository) {
        SecurityUtils.usersRepository = usersRepository;
    }

    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Попытка получить текущего пользователя без аутентификации");
            return null;
        }

        String username = authentication.getName();
        logger.debug("Получение текущего пользователя: {}", username);

        if (usersRepository == null) {
            logger.error("UsersRepository не инициализирован в SecurityUtils");
            return null;
        }

        return usersRepository.findByUsername(username);
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean canAccessUserData(Users resourceOwner) {
        Users currentUser = getCurrentUser();
        if (currentUser == null || resourceOwner == null) {
            return false;
        }

        boolean ownerAccess = resourceOwner.getId() != null && resourceOwner.getId().equals(currentUser.getId());
        boolean adminAccess = isAdmin();

        logger.debug("Проверка доступа: текущий={}, владелец={}, админ={}",
                currentUser.getUsername(),
                resourceOwner.getUsername(),
                adminAccess);

        return ownerAccess || adminAccess;
    }
}


