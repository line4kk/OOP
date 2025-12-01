package engine.service;

import engine.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import engine.repository.*;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
@Transactional(readOnly = true)
public class SortedSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SortedSearchService.class);

    @Autowired private UsersRepository usersRepository;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    public List<Users> findUsersSortedByUsername() {
        logger.debug("Поиск пользователей с сортировкой по username");
        List<Users> users = new ArrayList<>(usersRepository.findAll());
        users.sort(Comparator.comparing(user -> user.getUsername()));
        return users;
    }

    public List<Functions> findFunctionsSortedByName() {
        logger.debug("Поиск функций с сортировкой по имени");
        List<Functions> functions = new ArrayList<>(functionsRepository.findAll());
        functions.sort(Comparator.comparing(function -> function.getName()));
        return functions;
    }

    public List<Functions> findFunctionsSortedByType() {
        logger.debug("Поиск функций с сортировкой по типу");
        List<Functions> functions = new ArrayList<>(functionsRepository.findAll());
        functions.sort(Comparator.comparing(function -> function.getType()));
        return functions;
    }

    public List<FunctionPoints> findPointsSortedByX(Long functionId) {
        logger.debug("Поиск точек с сортировкой по X: {}", functionId);
        List<FunctionPoints> points = findPointsByFunction(functionId);
        points.sort(Comparator.comparingDouble(point -> point.getX_value()));
        return points;
    }

    public List<FunctionPoints> findPointsSortedByY(Long functionId) {
        logger.debug("Поиск точек с сортировкой по Y: {}", functionId);
        List<FunctionPoints> points = findPointsByFunction(functionId);
        points.sort(Comparator.comparingDouble(point -> point.getY_value()));
        return points;
    }

    private List<FunctionPoints> findPointsByFunction(Long functionId) {
        if (functionId == null || !functionsRepository.existsById(functionId)) {
            return Collections.emptyList();
        }
        return functionPointsRepository.findByFunction_Id(functionId);
    }
}
