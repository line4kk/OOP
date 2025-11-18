package service;

import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.*;

import java.util.Comparator;
import java.util.List;

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
        List<Users> users = usersRepository.findAll();
        users.sort(Comparator.comparing(Users::getUsername));
        return users;
    }

    public List<Functions> findFunctionsSortedByName() {
        logger.debug("Поиск функций с сортировкой по имени");
        List<Functions> functions = functionsRepository.findAll();
        functions.sort(Comparator.comparing(Functions::getName));
        return functions;
    }

    public List<Functions> findFunctionsSortedByType() {
        logger.debug("Поиск функций с сортировкой по типу");
        List<Functions> functions = functionsRepository.findAll();
        functions.sort(Comparator.comparing(Functions::getType));
        return functions;
    }

    public List<FunctionPoints> findPointsSortedByX(Long functionId) {
        logger.debug("Поиск точек с сортировкой по X: {}", functionId);
        return findPointsByFunction(functionId).stream()
                .sorted(Comparator.comparingDouble(FunctionPoints::getXValue))
                .toList();
    }

    public List<FunctionPoints> findPointsSortedByY(Long functionId) {
        logger.debug("Поиск точек с сортировкой по Y: {}", functionId);
        return findPointsByFunction(functionId).stream()
                .sorted(Comparator.comparingDouble(FunctionPoints::getYValue))
                .toList();
    }

    private List<FunctionPoints> findPointsByFunction(Long functionId) {
        return functionsRepository.findById(functionId)
                .map(functionPointsRepository::findByFunction)
                .orElse(List.of());
    }
}
