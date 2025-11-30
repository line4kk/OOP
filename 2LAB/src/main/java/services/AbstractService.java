package services;

import dao.DAO;
import model.dto.requests.FunctionCreateRequest;
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
}
