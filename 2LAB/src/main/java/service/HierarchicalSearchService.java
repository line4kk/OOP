package service;

import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class HierarchicalSearchService {

    private static final Logger logger = LoggerFactory.getLogger(HierarchicalSearchService.class);

    @Autowired private UsersRepository usersRepository;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;
    @Autowired private OperationsRepository operationsRepository;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    public Map<String, Object> findUserHierarchy(String username) {
        logger.debug("Поиск иерархии пользователя: {}", username);

        Map<String, Object> hierarchy = new HashMap<>();
        Users user = usersRepository.findByUsername(username);
        if (user == null) return hierarchy;

        hierarchy.put("user", user);

        List<Functions> functions = functionsRepository.findByUser(user);
        hierarchy.put("functions", functions);

        List<FunctionPoints> points = new ArrayList<>();
        for (Functions function : functions) {
            points.addAll(functionPointsRepository.findByFunction(function));
        }
        hierarchy.put("points", points);

        return hierarchy;
    }

    public Map<String, Object> findFunctionHierarchy(Long functionId) {
        logger.debug("Поиск иерархии функции: {}", functionId);

        Map<String, Object> hierarchy = new HashMap<>();
        Functions function = functionsRepository.findById(functionId).orElse(null);
        if (function == null) return hierarchy;

        hierarchy.put("function", function);

        List<FunctionPoints> points = functionPointsRepository.findByFunction(function);
        hierarchy.put("points", points);

        List<Operations> operations = new ArrayList<>();
        for (FunctionPoints point : points) {
            operations.addAll(operationsRepository.findByPoint1(point));
        }
        hierarchy.put("operations", operations);

        return hierarchy;
    }

}