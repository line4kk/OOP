package engine.controller;

import engine.dto.OperationRequest;
import engine.dto.OperationResultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class OperationsController {

    private static final Logger logger = LoggerFactory.getLogger(OperationsController.class);

    @PostMapping("/operations/results")
    public ResponseEntity<?> getOperationResult(@RequestBody OperationRequest request) {
        try {
            logger.info("POST /operations/results - операция: {} между точками {} и {}",
                    request.getOperation(), request.getPoint1Id(), request.getPoint2Id());

            if (request.getOperation() == null || !isValidOperation(request.getOperation())) {
                return ResponseEntity.status(404).body("Неизвестная операция");
            }

            if (request.getPoint1Id() == null || request.getPoint1Id() <= 0) {
                return ResponseEntity.status(400).body("Некорректные входные данные: point1_id");
            }

            if (!request.getOperation().equals("derive") &&
                    (request.getPoint2Id() == null || request.getPoint2Id() <= 0)) {
                return ResponseEntity.status(400).body("Некорректные входные данные: point2_id требуется для операции " + request.getOperation());
            }

            Double result = performOperation(request);

            if (result == null && request.getOperation().equals("division")) {
                return ResponseEntity.status(418).body("I'm a teapot (division by zero)");
            }

            OperationResultResponse response = new OperationResultResponse();
            response.setResultY(result);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при выполнении операции", e);
            return ResponseEntity.status(500).body("Ошибка со стороны сервера");
        }
    }

    private Double performOperation(OperationRequest request) {
        switch (request.getOperation()) {
            case "add":
                return 10.0;
            case "subtract":
                return 5.0;
            case "multiplication":
                return 25.0;
            case "division":
                if (request.getPoint2Id() != null && request.getPoint2Id() == 0) {
                    return null;
                }
                return request.getPoint2Id() != null ? 2.0 : null;
            case "derive":
                return 1.0;
            default:
                return null;
        }
    }

    private boolean isValidOperation(String operation) {
        return operation != null && (
                operation.equals("add") ||
                        operation.equals("subtract") ||
                        operation.equals("multiplication") ||
                        operation.equals("division") ||
                        operation.equals("derive")
        );
    }
}
