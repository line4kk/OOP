package services;

import dao.DAO;
import exceptions.AuthException;
import model.dto.responses.UserResponse;
import model.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractService<T extends DAO<?>> {
    protected final T dao;
    protected final Logger logger;


    public AbstractService(T dao) {
        this.dao = dao;
        logger = LoggerFactory.getLogger(getClass());
    }

    public T getDao() {
        return dao;
    }

    protected void ensureAccess(UserResponse user, long resourceOwnerId) {
        if (user == null || user.getRole() == null || user.getId() == null) {
            logger.warn("Ошибка при проверке полномочий пользователя {} к ресурсу, владельцем которого является пользователь id={}", user, resourceOwnerId);
            throw new AuthException("Forbidden");
        }

        boolean isAdmin = UserRole.ADMIN.getValue().equals(user.getRole());
        logger.info("Попытка получения доступа пользователя с ролью {}, id={} к ресурсу, владельцем которого является пользователь id={}", user.getRole(), user.getId(), resourceOwnerId);
        if (!isAdmin && user.getId() != resourceOwnerId) {
            logger.warn("Запрос отклонен");
            throw new AuthException("Forbidden");
        }
        logger.info("Успешно");
    }
}
