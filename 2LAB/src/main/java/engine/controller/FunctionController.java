package engine.controller;

import engine.dto.*;
import engine.entity.CompositeFunctionElements;
import engine.entity.Functions;
import engine.entity.FunctionPoints;
import engine.entity.Users;
import engine.repository.FunctionsRepository;
import engine.repository.FunctionPointsRepository;
import engine.service.SingleSearchService;
import engine.service.MultipleSearchService;
import engine.service.SortedSearchService;
import functions.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FunctionController {

    private static final Logger logger = LoggerFactory.getLogger(FunctionController.class);

    @Autowired private SingleSearchService singleSearchService;
    @Autowired private MultipleSearchService multipleSearchService;
    @Autowired private SortedSearchService sortedSearchService;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;

    @GetMapping("/functions")
    public ResponseEntity<?> getAllUserFunctions() {
        try {
            logger.info("GET /functions - получение всех функций пользователя");
            List<Functions> functions = multipleSearchService.findAllFunctions();
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

    @PostMapping("/functions")
    public ResponseEntity<?> createFunction(@RequestBody FunctionCreateRequest request) {
        try {
            logger.info("POST /functions - создание функции: {}", request.getName());

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя функции не может быть пустым");
            }

            if (request.getType() == null || !isValidFunctionType(request.getType())) {
                return ResponseEntity.status(400).body("Некорректный тип функции");
            }

            if (request.getSource() == null || !isValidFunctionSource(request.getSource())) {
                return ResponseEntity.status(400).body("Некорректный источник функции");
            }

            List<Functions> existingFunctions = multipleSearchService.findFunctionsByName(request.getName());
            if (!existingFunctions.isEmpty()) {
                return ResponseEntity.status(409).body("Функция с таким именем уже существует");
            }

            List<Users> users = multipleSearchService.findAllUsers();
            if (users.isEmpty()) {
                return ResponseEntity.status(400).body("Нет пользователей в системе");
            }

            Users currentUser = users.get(0);
            Functions newFunction = new Functions(currentUser, request.getName(), request.getType(), request.getSource());
            Functions savedFunction = functionsRepository.save(newFunction);

            FunctionResponse response = mapToFunctionResponse(savedFunction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/functions/{function_id}")
    public ResponseEntity<?> getFunctionInfo(@PathVariable("function_id") Long functionId) {
        try {
            logger.info("GET /functions/{} - получение информации о функции", functionId);

            if (functionId == null || functionId <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }

            return singleSearchService.findFunctionById(functionId)
                    .map(function -> ResponseEntity.ok(mapToFunctionResponse(function)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    @GetMapping("/functions/{function_id}/points")
    public ResponseEntity<?> getFunctionPoints(@PathVariable("function_id") Long functionId) {
        try {
            logger.info("GET /functions/{}/points - получение точек функции", functionId);

            if (functionId == null || functionId <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }

            if (!singleSearchService.findFunctionById(functionId).isPresent()) {
                return ResponseEntity.status(404).body("Функция не найдена");
            }

            List<FunctionPoints> points = multipleSearchService.findPointsByFunction(functionId);
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
            @PathVariable("function_id") Long functionId,
            @RequestBody List<PointRequest> pointsRequest) {
        try {
            logger.info("POST /functions/{}/points - добавление {} точек", functionId, pointsRequest.size());

            if (functionId == null || functionId <= 0) {
                return ResponseEntity.status(400).body("Некорректный ID функции");
            }

            if (pointsRequest == null || pointsRequest.isEmpty()) {
                return ResponseEntity.status(400).body("Список точек не может быть пустым");
            }

            for (PointRequest point : pointsRequest) {
                if (point.getX() == null) {
                    return ResponseEntity.status(400).body("Ошибка. Проверьте, что у каждой точки есть значение x");
                }
                if (point.getY() == null) {
                    return ResponseEntity.status(400).body("Ошибка. Проверьте, что у каждой точки есть значение y");
                }
            }

            Functions function = singleSearchService.findFunctionById(functionId).orElse(null);
            if (function != null) {
                for (PointRequest newPoint : pointsRequest) {
                    boolean pointExists = function.getPoints().stream()
                            .anyMatch(existingPoint -> existingPoint.getXValue().equals(newPoint.getX()));

                    if (pointExists) {
                        return ResponseEntity.status(409).body("Добавление существующей точки");
                    }
                }
            }

            List<PointResponse> response = pointsRequest.stream()
                    .map(point -> {
                        FunctionPoints newPoint = new FunctionPoints(function, point.getX(), point.getY());
                        FunctionPoints savedPoint = functionPointsRepository.save(newPoint);
                        return new PointResponse(savedPoint.getFunction().getId(), savedPoint.getXValue(), savedPoint.getYValue());
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при добавлении точек", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private FunctionResponse mapToFunctionResponse(Functions function) {
        return new FunctionResponse(function.getId(), function.getName(), function.getType(), function.getSource());
    }

    private PointResponse mapToPointResponse(FunctionPoints point) {
        return new PointResponse(point.getFunction().getId(), point.getXValue(), point.getYValue());
    }

    private boolean isValidFunctionType(String type) {
        return type != null && (
                type.equals("linked_list_tabulated") ||
                        type.equals("array_tabulated") ||
                        type.equals("analytical")
        );
    }

    private boolean isValidFunctionSource(String source) {
        return source != null && (
                source.equals("base") ||
                        source.equals("operation") ||
                        source.equals("composite")
        );
    }


    @Data
    public static class SamplingRequest {
        private Long analyticalFunctionId;
        private Double xFrom;
        private Double xTo;
        private Integer count;
    }
}