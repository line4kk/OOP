package engine.controller;

import engine.dto.CompositeCreateRequest;
import engine.dto.FunctionResponse;
import engine.entity.Functions;
import engine.service.MultipleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
public class AnalyticalFunctionController {

    @Autowired private MultipleSearchService multipleSearchService;
    private static final Logger logger = LoggerFactory.getLogger(AnalyticalFunctionController.class);

    @PostMapping("/analytical_functions")
    public ResponseEntity<?> createCompositeFunction(@RequestBody CompositeCreateRequest request) {
        try {
            logger.info("POST /analytical_functions - создание композитной функции: {}", request.getName());

            if (request.getFunctionIdsInOrder() == null || request.getFunctionIdsInOrder().size() < 2) {
                return ResponseEntity.status(400).body("Ошибка. Некорректные данные: требуется минимум 2 функции");
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Имя функции не может быть пустым");
            }
            if (request.getName().length() > 50) {
                return ResponseEntity.status(400).body("Имя функции не может превышать 50 символов");
            }

            for (Long functionId : request.getFunctionIdsInOrder()) {
                if (functionId == null || functionId <= 0) {
                    return ResponseEntity.status(400).body("Некорректный ID функции в списке");
                }
            }
            if (request.getName() != null) {
                List<Functions> existingFunctions = multipleSearchService.findFunctionsByName(request.getName());
                if (!existingFunctions.isEmpty()) {
                    return ResponseEntity.status(409).body("Композиция с таким именем уже существует");
                }
            }

            FunctionResponse response = new FunctionResponse(1L, request.getName(), "analytical", "composite");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании композитной функции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }
}
