package service;

import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.*;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SingleSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SingleSearchService.class);

    @Autowired private UsersRepository usersRepository;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;
    @Autowired private OperationsRepository operationsRepository;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    public Users findUserByUsername(String username) {
        logger.debug("Одиночный поиск пользователя: {}", username);
        return usersRepository.findByUsername(username);
    }

    public Optional<Functions> findFunctionById(Long id) {
        logger.debug("Одиночный поиск функции по ID: {}", id);
        return functionsRepository.findById(id);
    }

    public Optional<FunctionPoints> findPointById(Long id) {
        logger.debug("Одиночный поиск точки по ID: {}", id);
        return functionPointsRepository.findById(id);
    }

    public Optional<Operations> findOperationById(Long id) {
        logger.debug("Одиночный поиск операции по ID: {}", id);
        return operationsRepository.findById(id);
    }

    public Optional<CompositeFunctionElements> findCompositeElementById(Long id) {
        logger.debug("Одиночный поиск композитного элемента по ID: {}", id);
        return compositeFunctionElementsRepository.findById(id);
    }
}
