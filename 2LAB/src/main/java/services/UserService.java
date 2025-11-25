package services;

import dao.UserDAO;
import exceptions.AlreadyExistsException;
import exceptions.AuthException;
import model.User;
import model.dto.requests.UserLoginRequest;
import model.dto.requests.UserRegisterRequest;
import model.dto.responses.UserResponse;
import model.enums.FactoryType;

import org.mindrot.jbcrypt.BCrypt;

import java.util.NoSuchElementException;


public class UserService extends AbstractService<UserDAO> {

    public UserService() {
        super(new UserDAO());
    }

    public UserResponse getUserInfo(String username) {
        logger.debug("Получение информации о пользователе с username = {}", username);
        User user = dao.select(username);
        if (user != null) {
            return UserResponse.from(user);
        }
        logger.debug("Пользователь {} не найден", username);
        throw new NoSuchElementException("Информация о пользователе не найдена");
    }

    public UserResponse getUserInfo(long userId) {
        logger.debug("Получение информации о пользователе с userId = {}", userId);
        User user = dao.select(userId);
        if (user != null) {
            return UserResponse.from(user);
        }
        logger.debug("Пользователь userId = {} не найден", userId);
        throw new NoSuchElementException("Информация о пользователе не найдена");
    }

    public UserResponse setFactoryType(long userId, String newFactoryType) {
        if (FactoryType.isValidType(newFactoryType)) {
            dao.setFactoryType(userId, newFactoryType);
            logger.debug("Изменена фабрика у пользователя с id = {} на {}", userId, newFactoryType);
            return getUserInfo(userId);
        }
        logger.warn("Ошибка при изменении роли у пользователя с id = {} на {}", userId, newFactoryType);
        throw new IllegalArgumentException("Некорректный тип фабрики: " + newFactoryType);
    }

    public UserResponse signIn(UserLoginRequest userLoginRequest) {
        String plainPassword = userLoginRequest.getPassword();

        User userFromDatabase = dao.selectCredentials(userLoginRequest.getUsername());

        if (userFromDatabase != null && BCrypt.checkpw(plainPassword, userFromDatabase.getPasswordHash())) {
            logger.info("Пользователь {} успешно авторизован", userFromDatabase);
            return UserResponse.from(userFromDatabase);
        }

        logger.info("Неудачная авторизация. Username = {}", userLoginRequest.getUsername());
        throw new AuthException("Неправильный логин или пароль");
    }

    public UserResponse signUp(UserRegisterRequest userRegisterRequest) {
        User userFromRequest = userRegisterRequest.toEntity();
        if (dao.select(userFromRequest.getUsername()) != null) {
            logger.debug("Попытка регистрации с занятым именем пользователя {}", userFromRequest.getUsername());
            throw new AlreadyExistsException("Имя пользователя уже занято");
        }

        int passwordLength = userRegisterRequest.getPassword().length();
        if (passwordLength < 8 || passwordLength > 64) {
            logger.warn("Введена некорректная длина пароля при регистрации");
            throw new IllegalArgumentException("Ошибка. Некорректные данные");
        }

        User newUser = new User(
                userFromRequest.getUsername(),
                BCrypt.hashpw(userRegisterRequest.getPassword(), BCrypt.gensalt()),
                userFromRequest.getRole(),
                userFromRequest.getFactoryType()
        );

        dao.insert(newUser);
        logger.info("Пользователь {} успешно зарегистрирован", newUser.getUsername());
        return UserResponse.from(dao.select(newUser.getUsername()));
    }
}
