package engine.controller;

import engine.dto.FunctionCreateRequest;
import engine.dto.FunctionResponse;
import engine.dto.FunctionsResponse;
import engine.dto.PointRequest;
import engine.dto.PointResponse;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);
    private static final Set<String> VALID_FUNCTION_TYPES = Set.of("linked_list_tabulated", "array_tabulated", "analytical");
    private static final Set<String> VALID_FUNCTION_SOURCES = Set.of("base", "operation", "composite");

    @Autowired private SingleSearchService singleSearchService;
    @Autowired private MultipleSearchService multipleSearchService;
    @Autowired private SecurityUtils securityUtils;

    @GetMapping("/functions")
    public ResponseEntity<?> getAllUserFunctions() {
        try {
            logger.info("GET /functions - получение всех функций пользователя");

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<Functions> functions = multipleSearchService.findFunctionsByUser(currentUser.getUsername());
            FunctionsResponse response = new FunctionsResponse(
                    functions.stream().map(this::mapToFunctionResponse).collect(Collectors.toList())
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении функций", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/functions/analytical_functions")
    public ResponseEntity<?> getAllUserAnalyticFunctions() {
        try {
            logger.info("GET /functions/analytical_functions - получение аналитических функций пользователя");

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<Functions> analyticFunctions = multipleSearchService.findFunctionsByUser(currentUser.getUsername())
                    .stream()
                    .filter(f -> "analytical".equals(f.getType()))
                    .collect(Collectors.toList());

            FunctionsResponse response = new FunctionsResponse(
                    analyticFunctions.stream().map(this::mapToFunctionResponse).collect(Collectors.toList())
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении аналитических функций", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/functions")
    public ResponseEntity<?> createFunction(@RequestBody FunctionCreateRequest request) {
        try {
            logger.info("POST /functions - создание функции: {}", request.getName());

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getName() == null || request.getName().trim().isEmpty() ||
                    request.getType() == null || request.getSource() == null ||
                    !VALID_FUNCTION_TYPES.contains(request.getType()) ||
                    !VALID_FUNCTION_SOURCES.contains(request.getSource())) {
                return ResponseEntity.status(400).body("Bad Request");
            }

            boolean nameExists = multipleSearchService.findFunctionsByName(request.getName()).stream()
                    .anyMatch(f -> f.getUser().getId().equals(currentUser.getId()));
            if (nameExists) {
                return ResponseEntity.status(409).body("Функция с таким именем уже существует");
            }

            Functions savedFunction = singleSearchService.saveFunction(
                    new Functions(currentUser, request.getName(), request.getType(), request.getSource())
            );
            return ResponseEntity.ok(mapToFunctionResponse(savedFunction));
        } catch (Exception e) {
            logger.error("Ошибка при создании функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/functions/{function_id}")
    public ResponseEntity<?> getFunctionById(@PathVariable("function_id") Long functionId) {
        try {
            logger.info("GET /functions/{} - получение информации о функции", functionId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> functionOpt = singleSearchService.findFunctionById(functionId);
            if (functionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = functionOpt.get();
            if (!securityUtils.canAccessUserData(function.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            return ResponseEntity.ok(mapToFunctionResponse(function));
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PutMapping("/functions/{function_id}")
    public ResponseEntity<?> updateFunction(@PathVariable("function_id") Long functionId,
                                            @RequestBody FunctionCreateRequest request) {
        try {
            logger.info("PUT /functions/{} - обновление функции", functionId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> functionOpt = singleSearchService.findFunctionById(functionId);
            if (functionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = functionOpt.get();
            if (!securityUtils.canAccessUserData(function.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getName() == null || request.getType() == null || request.getSource() == null ||
                    !VALID_FUNCTION_TYPES.contains(request.getType()) ||
                    !VALID_FUNCTION_SOURCES.contains(request.getSource())) {
                return ResponseEntity.status(400).body("Bad Request");
            }

            boolean nameExists = multipleSearchService.findFunctionsByName(request.getName()).stream()
                    .anyMatch(f -> securityUtils.canAccessUserData(f.getUser()) && !f.getId().equals(functionId));
            if (nameExists) {
                return ResponseEntity.status(409).body("Функция с таким именем уже существует");
            }

            function.setName(request.getName());
            function.setType(request.getType());
            function.setSource(request.getSource());

            return ResponseEntity.ok(mapToFunctionResponse(singleSearchService.saveFunction(function)));
        } catch (Exception e) {
            logger.error("Ошибка при обновлении функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @DeleteMapping("/functions/{function_id}")
    public ResponseEntity<?> deleteFunction(@PathVariable("function_id") Long functionId) {
        try {
            logger.info("DELETE /functions/{} - удаление функции", functionId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> functionOpt = singleSearchService.findFunctionById(functionId);
            if (functionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = functionOpt.get();
            if (!securityUtils.canAccessUserData(function.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }
            if (singleSearchService.isFunctionUsedInComposition(function)) {
                return ResponseEntity.status(405).body("Невозможно удалить функцию, так как она используется в композиции");
            }

            singleSearchService.deleteFunction(functionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/functions/{function_id}/points")
    public ResponseEntity<?> getFunctionPoints(@PathVariable("function_id") Long functionId) {
        try {
            logger.info("GET /functions/{}/points - получение точек функции", functionId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> functionOpt = singleSearchService.findFunctionById(functionId);
            if (functionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = functionOpt.get();
            if (!securityUtils.canAccessUserData(function.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<PointResponse> response = multipleSearchService.findPointsByFunction(functionId).stream()
                    .sorted(Comparator.comparing(FunctionPoints::getId))
                    .map(this::mapToPointResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении точек функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/functions/{function_id}/points")
    public ResponseEntity<?> addFunctionPoints(@PathVariable("function_id") Long functionId,
                                               @RequestBody List<PointRequest> pointRequests) {
        try {
            logger.info("POST /functions/{}/points - добавление точек", functionId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> functionOpt = singleSearchService.findFunctionById(functionId);
            if (functionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = functionOpt.get();
            if (!securityUtils.canAccessUserData(function.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (pointRequests == null || pointRequests.isEmpty()) {
                return ResponseEntity.status(400).body("Список точек не может быть пустым");
            }

            Set<Double> newXValues = new HashSet<>();
            for (PointRequest point : pointRequests) {
                if (point.getX() == null || point.getY() == null) {
                    return ResponseEntity.status(400).body("Ошибка. Проверьте, что у каждой точки есть значения x и y");
                }
                if (!newXValues.add(point.getX())) {
                    return ResponseEntity.status(409).body("Добавление существующей точки");
                }
                if (singleSearchService.findFunctionPointByX(functionId, point.getX()).isPresent()) {
                    return ResponseEntity.status(409).body("Добавление существующей точки");
                }
            }

            List<PointResponse> response = new ArrayList<>();
            for (PointRequest point : pointRequests) {
                FunctionPoints savedPoint = singleSearchService.saveFunctionPoint(
                        new FunctionPoints(function, point.getX(), point.getY())
                );
                response.add(mapToPointResponse(savedPoint));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при добавлении точек", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PutMapping("/functions/{function_id}/points/{point_id}")
    public ResponseEntity<?> updateFunctionPoint(@PathVariable("function_id") Long functionId,
                                                 @PathVariable("point_id") Long pointId,
                                                 @RequestBody PointRequest pointRequest) {
        try {
            logger.info("PUT /functions/{}/points/{} - обновление точки", functionId, pointId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> functionOpt = singleSearchService.findFunctionById(functionId);
            if (functionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = functionOpt.get();
            if (!securityUtils.canAccessUserData(function.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<FunctionPoints> pointOpt = singleSearchService.findFunctionPointByFunction(pointId, functionId);
            if (pointOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Точка не найдена");
            }

            if (pointRequest.getX() == null || pointRequest.getY() == null) {
                return ResponseEntity.status(400).body("Ошибка. Проверьте, что у каждой точки есть значения x и y");
            }

            Optional<FunctionPoints> duplicate = singleSearchService.findFunctionPointByX(functionId, pointRequest.getX());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(pointId)) {
                return ResponseEntity.status(409).body("Добавление существующей точки");
            }

            FunctionPoints point = pointOpt.get();
            point.setX_value(pointRequest.getX());
            point.setYValue(pointRequest.getY());

            return ResponseEntity.ok(mapToPointResponse(singleSearchService.saveFunctionPoint(point)));
        } catch (Exception e) {
            logger.error("Ошибка при обновлении точки", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @DeleteMapping("/functions/{function_id}/points/{point_id}")
    public ResponseEntity<?> deleteFunctionPoint(@PathVariable("function_id") Long functionId,
                                                 @PathVariable("point_id") Long pointId) {
        try {
            logger.info("DELETE /functions/{}/points/{} - удаление точки", functionId, pointId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> functionOpt = singleSearchService.findFunctionById(functionId);
            if (functionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = functionOpt.get();
            if (!securityUtils.canAccessUserData(function.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<FunctionPoints> pointOpt = singleSearchService.findFunctionPointByFunction(pointId, functionId);
            if (pointOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Точка не найдена");
            }

            singleSearchService.deleteFunctionPoint(pointOpt.get());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении точки функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @DeleteMapping("/functions/{function_id}/points")
    public ResponseEntity<?> deleteAllFunctionPoints(@PathVariable("function_id") Long functionId) {
        try {
            logger.info("DELETE /functions/{}/points - удаление всех точек функции", functionId);

            Users currentUser = securityUtils.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> functionOpt = singleSearchService.findFunctionById(functionId);
            if (functionOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = functionOpt.get();
            if (!securityUtils.canAccessUserData(function.getUser())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<FunctionPoints> points = multipleSearchService.findPointsByFunction(functionId);
            for (FunctionPoints point : points) {
                singleSearchService.deleteFunctionPoint(point);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении точек функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private FunctionResponse mapToFunctionResponse(Functions function) {
        return new FunctionResponse(function.getId(), function.getName(), function.getType(), function.getSource());
    }

    private PointResponse mapToPointResponse(FunctionPoints point) {
        return new PointResponse(point.getId(), point.getX_value(), point.getY_value());
    }
}