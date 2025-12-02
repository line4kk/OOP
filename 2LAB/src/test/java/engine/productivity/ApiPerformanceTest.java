package engine.productivity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ApiPerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseUrl = "http://localhost:3000";
    private final String username = "testuser_perf_" + System.currentTimeMillis();
    private final String password = "testpass123";

    private List<String[]> results = new ArrayList<>();

    @Test
    public void testAllApiEndpoints() {
        // Создаем отдельный TestRestTemplate без авторизации для публичных запросов
        TestRestTemplate publicRestTemplate = new TestRestTemplate();

        results.add(new String[]{"API_Operation", "Time(ms)", "Status"});

        final Long[] functionIds = {null, null, null}; // Для 3 функций
        final String[] basicAuthHeader = {null};

        try {
            // 1. Регистрация пользователя
            timeMeasurement("User_Registration", () -> {
                String requestJson = "{" +
                        "\"username\": \"" + username + "\"," +
                        "\"password\": \"" + password + "\"," +
                        "\"role\": \"user\"," +
                        "\"factory_type\": \"array\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/users/register", request, String.class);

                System.out.println("=== REGISTRATION RESPONSE ===");
                System.out.println("Status: " + response.getStatusCode());
                System.out.println("Body: " + response.getBody());

                return getStatusFromResponse(response);
            });

            // 2. Аутентификация через /users/auth
            String authStatus = timeMeasurement("User_Authentication", () -> {
                String requestJson = "{" +
                        "\"username\": \"" + username + "\"," +
                        "\"password\": \"" + password + "\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/users/auth", request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    // Создаем Basic Auth header для последующих запросов
                    String credentials = username + ":" + password;
                    basicAuthHeader[0] = "Basic " +
                            java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
                    return "SUCCESS";
                } else {
                    System.out.println("=== AUTH FAILED ===");
                    System.out.println("Status: " + response.getStatusCode());
                    System.out.println("Body: " + response.getBody());
                    return getStatusFromResponse(response);
                }
            });

            if (!"SUCCESS".equals(authStatus)) {
                System.out.println("Authentication failed, skipping remaining tests");
                saveResults();
                return;
            }

            System.out.println("=== BASIC AUTH HEADER ===");
            System.out.println(basicAuthHeader[0]);

            // 3. Получение информации о пользователе (с Basic Auth)
            timeMeasurement("Get_Current_User", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/users", HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 4. Обновление типа фабрики
            timeMeasurement("Update_Factory_Type", () -> {
                String requestJson = "{" +
                        "\"factory_type\": \"linked_list\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/users/settings/factory_types",
                        HttpMethod.PUT, request, String.class);

                return getStatusFromResponse(response);
            });

            // 5. Создание функции 1 (linked_list_tabulated)
            functionIds[0] = timeMeasurementWithId("Create_Function_1", () -> {
                String requestJson = "{" +
                        "\"name\": \"TabulatedFunc_1_" + System.currentTimeMillis() + "\"," +
                        "\"type\": \"linked_list_tabulated\"," +
                        "\"source\": \"base\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions", request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return extractIdFromResponse(response.getBody());
                }
                return null;
            });

            // 6. Создание функции 2 (linked_list_tabulated)
            functionIds[1] = timeMeasurementWithId("Create_Function_2", () -> {
                String requestJson = "{" +
                        "\"name\": \"TabulatedFunc_2_" + System.currentTimeMillis() + "\"," +
                        "\"type\": \"linked_list_tabulated\"," +
                        "\"source\": \"base\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions", request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return extractIdFromResponse(response.getBody());
                }
                return null;
            });

            // 7. Создание функции 3 (analytical)
            functionIds[2] = timeMeasurementWithId("Create_Function_3", () -> {
                String requestJson = "{" +
                        "\"name\": \"AnalyticFunc_3_" + System.currentTimeMillis() + "\"," +
                        "\"type\": \"analytical\"," +
                        "\"source\": \"base\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions", request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return extractIdFromResponse(response.getBody());
                }
                return null;
            });

            if (functionIds[0] == null || functionIds[1] == null || functionIds[2] == null) {
                System.out.println("Function creation failed");
                saveResults();
                return;
            }

            final Long funcId1 = functionIds[0];
            final Long funcId2 = functionIds[1];
            final Long funcId3 = functionIds[2];

            // 8. Получение всех функций
            timeMeasurement("Get_All_Functions", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions", HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 9. Получение аналитических функций
            timeMeasurement("Get_Analytic_Functions", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/analytical_functions",
                        HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 10. Получение информации о функции
            timeMeasurement("Get_Function_Info", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1,
                        HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 11. Обновление функции
            timeMeasurement("Update_Function", () -> {
                String requestJson = "{" +
                        "\"name\": \"UpdatedFunction_Perf\"," +
                        "\"type\": \"linked_list_tabulated\"," +
                        "\"source\": \"base\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1,
                        HttpMethod.PUT, request, String.class);

                return getStatusFromResponse(response);
            });

            // 12. Добавление точек функции 1
            timeMeasurement("Add_Function_Points", () -> {
                String requestJson = "[" +
                        "{\"x\": 1.0, \"y\": 2.0}," +
                        "{\"x\": 2.0, \"y\": 4.0}," +
                        "{\"x\": 3.0, \"y\": 6.0}," +
                        "{\"x\": -5.5, \"y\": -11.0}," +
                        "{\"x\": 100.25, \"y\": 200.5}" +
                        "]";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions/" + funcId1 + "/points",
                        request, String.class);

                return getStatusFromResponse(response);
            });

            // 13. Добавление точек функции 2
            timeMeasurement("Add_Points_Function2", () -> {
                String requestJson = "[" +
                        "{\"x\": 0.5, \"y\": 1.0}," +
                        "{\"x\": 1.5, \"y\": 3.0}," +
                        "{\"x\": 2.5, \"y\": 5.0}" +
                        "]";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions/" + funcId2 + "/points",
                        request, String.class);

                return getStatusFromResponse(response);
            });

            // 14. Получение точек функции
            timeMeasurement("Get_Function_Points", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1 + "/points",
                        HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 15. Обновление точки (сначала получим ID точки)
            String pointId = extractPointIdFromResponse(funcId1, basicAuthHeader[0], publicRestTemplate);

            if (pointId != null) {
                timeMeasurement("Update_Point", () -> {
                    String requestJson = "{" +
                            "\"x\": 15.0," +
                            "\"y\": 30.0" +
                            "}";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Authorization", basicAuthHeader[0]);
                    HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                    ResponseEntity<String> response = publicRestTemplate.exchange(
                            baseUrl + "/functions/" + funcId1 + "/points/" + pointId,
                            HttpMethod.PUT, request, String.class);

                    return getStatusFromResponse(response);
                });

                // 16. Удаление точки
                timeMeasurement("Delete_Point", () -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", basicAuthHeader[0]);
                    HttpEntity<String> request = new HttpEntity<>(headers);

                    ResponseEntity<String> response = publicRestTemplate.exchange(
                            baseUrl + "/functions/" + funcId1 + "/points/" + pointId,
                            HttpMethod.DELETE, request, String.class);

                    return getStatusFromResponse(response);
                });
            } else {
                results.add(new String[]{"Update_Point", "0.00", "SKIPPED"});
                results.add(new String[]{"Delete_Point", "0.00", "SKIPPED"});
            }

            // 17. Добавление точек снова
            timeMeasurement("Add_Points_Again", () -> {
                String requestJson = "[" +
                        "{\"x\": 10.0, \"y\": 20.0}," +
                        "{\"x\": 20.0, \"y\": 40.0}" +
                        "]";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions/" + funcId1 + "/points",
                        request, String.class);

                return getStatusFromResponse(response);
            });

            // 18. Удаление всех точек
            timeMeasurement("Delete_All_Points", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1 + "/points",
                        HttpMethod.DELETE, request, String.class);

                return getStatusFromResponse(response);
            });

            // 19. Создание композитной функции
            timeMeasurement("Create_Composite_Function", () -> {
                String requestJson = "{" +
                        "\"function_ids_in_order\": [" + funcId3 + "," + funcId2 + "]," +
                        "\"name\": \"CompositeFunction_Perf_" + System.currentTimeMillis() + "\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions/analytical_functions", request, String.class);

                return getStatusFromResponse(response);
            });

            // 20. Сэмплирование функции
            timeMeasurement("Function_Sampling", () -> {
                String requestJson = "{" +
                        "\"function_id\": " + funcId1 + "," +
                        "\"analytical_function_id\": " + funcId3 + "," +
                        "\"x_from\": 0," +
                        "\"x_to\": 10," +
                        "\"count\": 20" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions/" + funcId1 + "/sampling", request, String.class);

                return getStatusFromResponse(response);
            });

            // 21. Операция производной
            timeMeasurement("Perform_Operation_Derive", () -> {
                String requestJson = "{" +
                        "\"function1_id\": " + funcId1 + "," +
                        "\"function2_id\": null," +
                        "\"operation\": \"derive\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/operation", request, String.class);

                return getStatusFromResponse(response);
            });

            // 22. Удаление функции (только Function 1, не из композиции)
            timeMeasurement("Delete_Function", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1,
                        HttpMethod.DELETE, request, String.class);

                return getStatusFromResponse(response);
            });

            // Сохраняем результаты
            saveResults();

        } catch (Exception e) {
            System.err.println("Error during API testing: " + e.getMessage());
            e.printStackTrace();
            saveResults();
        }
    }

    private String extractPointIdFromResponse(Long funcId, String authHeader, TestRestTemplate restTemplate) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/functions/" + funcId + "/points",
                    HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String body = response.getBody();
                if (body.startsWith("[") && body.contains("\"id\":")) {
                    int idStart = body.indexOf("\"id\":") + 5;
                    int idEnd = body.indexOf(",", idStart);
                    if (idEnd == -1) idEnd = body.indexOf("}", idStart);
                    return body.substring(idStart, idEnd).trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract point ID: " + e.getMessage());
        }
        return null;
    }

    private String getStatusFromResponse(ResponseEntity<String> response) {
        int statusCode = response.getStatusCodeValue();
        switch (statusCode) {
            case 200: case 201: case 204:
                return "SUCCESS";
            case 400:
                return "BAD_REQUEST";
            case 401:
                return "UNAUTHORIZED";
            case 403:
                return "FORBIDDEN";
            case 404:
                return "NOT_FOUND";
            case 405:
                return "METHOD_NOT_ALLOWED";
            case 409:
                return "CONFLICT";
            case 418:
                return "TEAPOT";
            case 500:
                return "SERVER_ERROR";
            default:
                return "UNKNOWN_" + statusCode;
        }
    }

    private Long extractIdFromResponse(String responseBody) {
        if (responseBody != null) {
            try {
                // Парсим JSON более надежно
                if (responseBody.contains("\"id\"")) {
                    String[] parts = responseBody.split("\"id\":");
                    if (parts.length > 1) {
                        String idPart = parts[1];
                        String idStr = idPart.split(",")[0].trim();
                        // Убираем возможные кавычки и скобки
                        idStr = idStr.replace("\"", "").replace("}", "").trim();
                        return Long.parseLong(idStr);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse ID from response: " + responseBody);
                e.printStackTrace();
            }
        }
        return null;
    }

    private String timeMeasurement(String operationName, StatusOperation apiOperation) {
        long startTime = System.nanoTime();
        try {
            String status = apiOperation.execute();
            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1000000.0;
            results.add(new String[]{operationName, String.format("%.2f", timeMs), status});
            System.out.println(operationName + " - " + status + " in " + timeMs + "ms");
            return status;
        } catch (Exception e) {
            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1000000.0;
            results.add(new String[]{operationName, String.format("%.2f", timeMs), "EXCEPTION"});
            System.out.println(operationName + " - EXCEPTION in " + timeMs + "ms: " + e.getMessage());
            return "EXCEPTION";
        }
    }

    private Long timeMeasurementWithId(String operationName, IdOperation apiOperation) {
        long startTime = System.nanoTime();
        try {
            Long result = apiOperation.execute();
            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1000000.0;
            String status = result != null ? "SUCCESS" : "FAILED";
            results.add(new String[]{operationName, String.format("%.2f", timeMs), status});
            System.out.println(operationName + " - " + status + " in " + timeMs + "ms");
            return result;
        } catch (Exception e) {
            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1000000.0;
            results.add(new String[]{operationName, String.format("%.2f", timeMs), "EXCEPTION"});
            System.out.println(operationName + " - EXCEPTION in " + timeMs + "ms: " + e.getMessage());
            return null;
        }
    }

    private void saveResults() {
        try (FileWriter writer = new FileWriter("api_performance_results.csv")) {
            writer.write('\uFEFF');
            for (String[] record : results) {
                writer.write(String.join(",", record));
                writer.write("\n");
            }
            System.out.println("Results saved to api_performance_results.csv");

        } catch (IOException e) {
            System.err.println("Error writing CSV: " + e.getMessage());
        }
    }


    @FunctionalInterface
    private interface StatusOperation {
        String execute();
    }

    @FunctionalInterface
    private interface IdOperation {
        Long execute();
    }
}