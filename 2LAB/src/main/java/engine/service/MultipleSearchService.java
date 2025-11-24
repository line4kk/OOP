package engine.service;

import engine.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import engine.repository.*;

import java.util.List;
import java.util.ArrayList;

@Service
@Transactional(readOnly = true)
public class MultipleSearchService {

    private static final Logger logger = LoggerFactory.getLogger(MultipleSearchService.class);

    @Autowired private UsersRepository usersRepository;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;
    @Autowired private OperationsRepository operationsRepository;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    public List<Users> findAllUsers() {
        logger.debug("Множественный поиск всех пользователей");
        return usersRepository.findAll();
    }

    public List<Functions> findFunctionsByUser(String username) {
        logger.debug("Множественный поиск функций пользователя: {}", username);
        Users user = usersRepository.findByUsername(username);
        return user != null ? functionsRepository.findByUser(user) : new ArrayList<>();
    }

    public List<Functions> findFunctionsByType(String type) {
        logger.debug("Множественный поиск функций по типу: {}", type);
        return functionsRepository.findByType(type);
    }

    public List<FunctionPoints> findPointsByFunction(Long functionId) {
        logger.debug("Множественный поиск точек функции: {}", functionId);
        return functionsRepository.findById(functionId)
                .map(functionPointsRepository::findByFunction)
                .orElse(new ArrayList<>());
    }

    public List<Functions> findCompositeFunctions() {
        logger.debug("Множественный поиск композитных функций");
        return functionsRepository.findAll().stream()
                .filter(f -> "composite".equals(f.getSource()))
                .collect(java.util.stream.Collectors.toList());
    }
}