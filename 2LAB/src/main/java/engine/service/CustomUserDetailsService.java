package engine.service;

import engine.entity.Users;
import engine.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Загрузка пользователя: {}", username);

        Users user = usersRepository.findByUsername(username);
        if (user == null) {
            logger.warn("Пользователь не найден: {}", username);
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())
        );

        logger.debug("Успешная загрузка пользователя: {} с ролью: {}", username, user.getRole());

        return new User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }

    public Users loadUserEntityByUsername(String username) throws UsernameNotFoundException {
        logger.info("Загрузка сущности пользователя: {}", username);

        Users user = usersRepository.findByUsername(username);
        if (user == null) {
            logger.warn("Сущность пользователя не найдена: {}", username);
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        logger.debug("Успешная загрузка сущности пользователя: {}", username);
        return user;
    }
}