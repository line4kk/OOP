package engine.controller;

import engine.dto.*;
import engine.entity.Functions;
import engine.entity.FunctionPoints;
import engine.entity.Users;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);

    @Autowired private SingleSearchService singleSearchService;
    @Autowired private MultipleSearchService multipleSearchService;
    @Autowired private SecurityUtils securityUtils;

    @GetMapping("/functions")
    public ResponseEntity<?> getAllUserFunctions() {
        try {
            logger.info("GET /functions - получение всех функций пользователя");

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<Functions> functions = multipleSearchService.findFunctionsByUser(current_user.getUsername());
            FunctionsResponse response = new FunctionsResponse();
            response.setFunctions(functions.stream()
                    .map(this::mapToFunctionResponse)
                    .collect(Collectors.toList()));

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

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<Functions> all_functions = multipleSearchService.findFunctionsByUser(current_user.getUsername());
            List<Functions> analytic_functions = all_functions.stream()
                    .filter(f -> "analytical".equals(f.getType()))
                    .collect(Collectors.toList());

            FunctionsResponse response = new FunctionsResponse();
            response.setFunctions(analytic_functions.stream()
                    .map(this::mapToFunctionResponse)
                    .collect(Collectors.toList()));

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

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя функции не может быть пустым");
            }

            List<Functions> existing_functions = multipleSearchService.findFunctionsByName(request.getName());
            boolean name_exists = existing_functions.stream()
                    .anyMatch(f -> f.getUser().getId().equals(current_user.getId()));

            if (name_exists) {
                return ResponseEntity.status(409).body("Функция с таким именем уже существует");
            }

            Functions new_function = new Functions(current_user, request.getName(),
                    request.getType(), request.getSource());
            Functions saved_function = singleSearchService.saveFunction(new_function);

            FunctionResponse response = mapToFunctionResponse(saved_function);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/functions/{function_id}")
    public ResponseEntity<?> getFunctionInfo(@PathVariable("function_id") Long function_id) {
        try {
            logger.info("GET /functions/{} - получение информации о функции", function_id);

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Functions function = singleSearchService.findFunctionById(function_id).orElse(null);
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!function.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            return ResponseEntity.ok(mapToFunctionResponse(function));
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PutMapping("/functions/{function_id}")
    public ResponseEntity<?> updateFunction(
            @PathVariable("function_id") Long function_id,
            @RequestBody FunctionCreateRequest request) {
        try {
            logger.info("PUT /functions/{} - обновление функции", function_id);

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Functions function = singleSearchService.findFunctionById(function_id).orElse(null);
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!function.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                List<Functions> existing_functions = multipleSearchService.findFunctionsByName(request.getName());
                boolean name_exists = existing_functions.stream()
                        .anyMatch(f -> f.getUser().getId().equals(current_user.getId()) &&
                                !f.getId().equals(function_id));

                if (name_exists) {
                    return ResponseEntity.status(409).body("Функция с таким именем уже существует");
                }
                function.setName(request.getName());
            }

            Functions updated_function = singleSearchService.saveFunction(function);

            FunctionResponse response = mapToFunctionResponse(updated_function);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @DeleteMapping("/functions/{function_id}")
    public ResponseEntity<?> deleteFunction(@PathVariable("function_id") Long function_id) {
        try {
            logger.info("DELETE /functions/{} - удаление функции", function_id);

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Functions function = singleSearchService.findFunctionById(function_id).orElse(null);
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!function.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            singleSearchService.deleteFunction(function_id);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/functions/{function_id}/points")
    public ResponseEntity<?> getFunctionPoints(@PathVariable("function_id") Long function_id) {
        try {
            logger.info("GET /functions/{}/points - получение точек функции", function_id);

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Functions function = singleSearchService.findFunctionById(function_id).orElse(null);
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!function.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<FunctionPoints> points = multipleSearchService.findPointsByFunction(function_id);
            List<PointResponse> response = points.stream()
                    .map(this::mapToPointResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении точек функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @PostMapping("/functions/{function_id}/points")
    public ResponseEntity<?> addFunctionPoints(
            @PathVariable("function_id") Long function_id,
            @RequestBody List<PointRequest> points_request) {
        try {
            logger.info("POST /functions/{}/points - добавление {} точек", function_id, points_request.size());

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> function_opt = singleSearchService.findFunctionById(function_id);
            if (function_opt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = function_opt.get();
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!function.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            if (points_request == null || points_request.isEmpty()) {
                return ResponseEntity.status(400).body("Список точек не может быть пустым");
            }

            List<PointResponse> response = points_request.stream()
                    .map(point -> {
                        FunctionPoints new_point = new FunctionPoints(function, point.getX(), point.getY());

                        FunctionPoints saved_point = singleSearchService.saveFunctionPoint(new_point);
                        return new PointResponse(
                                generatePointId(saved_point),
                                saved_point.getX_value(),
                                saved_point.getY_value()
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при добавлении точек", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @DeleteMapping("/functions/{function_id}/points")
    public ResponseEntity<?> deleteAllFunctionPoints(@PathVariable("function_id") Long function_id) {
        try {
            logger.info("DELETE /functions/{}/points - удаление всех точек функции", function_id);

            Users current_user = securityUtils.getCurrentUser();
            if (current_user == null) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            Optional<Functions> function_opt = singleSearchService.findFunctionById(function_id);
            if (function_opt.isEmpty()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }
            Functions function = function_opt.get();
            if (function == null) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            if (!function.getUser().getId().equals(current_user.getId())) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            List<FunctionPoints> points = multipleSearchService.findPointsByFunction(function_id);
            for (FunctionPoints point : points) {
                singleSearchService.deleteFunctionPoint(point);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении точек функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private Long generatePointId(FunctionPoints point) {
        if (point.getFunction() == null || point.getX_value() == null) {
            return 0L;
        }
        long functionId = point.getFunction().getId();
        double xValue = point.getX_value();
        long xBits = Double.doubleToLongBits(xValue);
        long id = (functionId << 32) ^ (xBits >>> 32) ^ (xBits & 0xFFFFFFFFL);
        return id & Long.MAX_VALUE;
    }

    private FunctionResponse mapToFunctionResponse(Functions function) {
        return new FunctionResponse(function.getId(), function.getName(),
                function.getType(), function.getSource());
    }

    private PointResponse mapToPointResponse(FunctionPoints point) {
        return new PointResponse(generatePointId(point), point.getX_value(), point.getY_value());    }
}