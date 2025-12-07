package engine.service;

import engine.entity.CompositeFunctionElements;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import engine.repository.CompositeFunctionElementsRepository;
import engine.repository.FunctionPointsRepository;
import engine.repository.FunctionsRepository;
import engine.repository.UsersRepository;
import engine.service.AnalyticalFunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SingleSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SingleSearchService.class);

    @Autowired private UsersRepository usersRepository;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    public Users findUserByUsername(String username) {
        logger.debug("Поиск пользователя по имени: {}", username);
        return usersRepository.findByUsername(username);
    }

    public Users findUserById(Long id) {
        logger.debug("Поиск пользователя по ID: {}", id);
        return usersRepository.findById(id).orElse(null);
    }

    public Optional<Functions> findFunctionById(Long id) {
        logger.debug("Поиск функции по ID: {}", id);
        return functionsRepository.findById(id);
    }

    public Optional<Functions> findFunctionByNameAndUser(String name, Users user) {
        logger.debug("Поиск функции по имени и пользователю: {}, {}", name, user.getUsername());
        return functionsRepository.findByNameAndUser(name, user);
    }

    public boolean existsFunctionByNameAndUser(String name, Users user) {
        logger.debug("Проверка существования функции по имени и пользователю: {}, {}", name, user.getUsername());
        return functionsRepository.existsByNameAndUser(name, user);
    }

    public long countUsersByRole(String role) {
        logger.debug("Подсчет пользователей с ролью: {}", role);
        return usersRepository.countByRole(role);
    }

    @Transactional
    public Users saveUser(Users user) {
        logger.debug("Сохранение пользователя: {}", user.getUsername());
        return usersRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        logger.debug("Удаление пользователя с ID: {}", userId);
        usersRepository.deleteById(userId);
    }

    @Transactional
    public Functions saveFunction(Functions function) {
        return functionsRepository.save(function);
    }

    @Transactional
    public void deleteFunction(Long functionId) {
        functionsRepository.deleteById(functionId);
    }

    @Transactional
    public FunctionPoints saveFunctionPoint(FunctionPoints point) {
        logger.debug("Сохранение точки функции ID: {}", point.getFunction().getId());
        return functionPointsRepository.save(point);
    }

    public Optional<FunctionPoints> findFunctionPoint(Long pointId) {
        logger.debug("Поиск точки функции по ID: {}", pointId);
        return functionPointsRepository.findById(pointId);
    }

    public Optional<FunctionPoints> findFunctionPointByFunction(Long pointId, Long functionId) {
        logger.debug("Поиск точки {} для функции {}", pointId, functionId);
        return functionPointsRepository.findByIdAndFunction_Id(pointId, functionId);
    }

    public Optional<FunctionPoints> findFunctionPointByX(Long functionId, Double xValue) {
        logger.debug("Поиск точки функции {} по x={}", functionId, xValue);
        return functionPointsRepository.findByFunctionIdAndX(functionId, xValue);
    }

    @Transactional
    public void deleteFunctionPoint(FunctionPoints point) {
        logger.debug("Удаление точки функции ID: {}, x: {}", point.getFunction().getId(), point.getX_value());
        functionPointsRepository.delete(point);
    }

    @Transactional
    public CompositeFunctionElements saveCompositeFunctionElement(CompositeFunctionElements element) {
        logger.debug("Сохранение элемента композитной функции");
        return compositeFunctionElementsRepository.save(element);
    }

    public boolean isFunctionUsedInComposition(Functions function) {
        return !compositeFunctionElementsRepository.findByFunction(function).isEmpty();
    }
}
