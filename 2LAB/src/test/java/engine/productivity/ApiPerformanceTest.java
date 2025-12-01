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

        final Long[] functionIds = {null, null};
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

            // 4. Обновление типа фабрики (исправлено: factory_type вместо factoryType)
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
                        HttpMethod.PATCH, request, String.class);

                return getStatusFromResponse(response);
            });

            // 5. Создание функции 1
            functionIds[0] = timeMeasurementWithId("Create_Function_1", () -> {
                String requestJson = "{" +
                        "\"name\": \"TabulatedFunc_1_" + System.currentTimeMillis() + "\"," +
                        "\"type\": \"array_tabulated\"," +
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

            // 6. Создание функции 2
            functionIds[1] = timeMeasurementWithId("Create_Function_2", () -> {
                String requestJson = "{" +
                        "\"name\": \"AnalyticFunc_2_" + System.currentTimeMillis() + "\"," +
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

            if (functionIds[0] == null || functionIds[1] == null) {
                System.out.println("Function creation failed");
                saveResults();
                return;
            }

            final Long funcId1 = functionIds[0];
            final Long funcId2 = functionIds[1];

            // 7. Получение всех функций
            timeMeasurement("Get_All_Functions", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions", HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 8. Получение аналитических функций
            timeMeasurement("Get_Analytic_Functions", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/analytical_functions",
                        HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 9. Получение информации о функции
            timeMeasurement("Get_Function_Info", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1,
                        HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 10. Обновление функции - исправлено на PUT
            timeMeasurement("Update_Function", () -> {
                String requestJson = "{" +
                        "\"name\": \"UpdatedFunction_Perf\"," +
                        "\"type\": \"array_tabulated\"," +
                        "\"source\": \"base\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                // Используем PUT вместо POST
                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1,
                        HttpMethod.PUT, request, String.class);

                return getStatusFromResponse(response);
            });

            // 11. Добавление точек функции
            timeMeasurement("Add_Function_Points", () -> {
                String requestJson = "[" +
                        "{\"x\": 1.0, \"y\": 2.0}," +
                        "{\"x\": 2.0, \"y\": 4.0}," +
                        "{\"x\": 3.0, \"y\": 6.0}" +
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

            // 12. Получение точек функции
            timeMeasurement("Get_Function_Points", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1 + "/points",
                        HttpMethod.GET, request, String.class);

                return getStatusFromResponse(response);
            });

            // 13-14. Пропускаем обновление и удаление точки (нужен ID точки)
            results.add(new String[]{"Update_Point", "0.00", "SKIPPED"});
            results.add(new String[]{"Delete_Point", "0.00", "SKIPPED"});

            // 15. Добавление точек снова
            timeMeasurement("Add_Points_Again", () -> {
                String requestJson = "[" +
                        "{\"x\": 1.0, \"y\": 2.0}," +
                        "{\"x\": 2.0, \"y\": 4.0}" +
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

            // 16. Удаление всех точек
            timeMeasurement("Delete_All_Points", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId1 + "/points",
                        HttpMethod.DELETE, request, String.class);

                return getStatusFromResponse(response);
            });

            // 17. Создание композитной функции - исправлено на function_ids_in_order
            timeMeasurement("Create_Composite_Function", () -> {
                String requestJson = "{" +
                        "\"function_ids_in_order\": [" + funcId1 + "," + funcId2 + "]," +
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

            // 18. Сэмплирование функции
            timeMeasurement("Function_Sampling", () -> {
                String requestJson = "{" +
                        "\"function_id\": " + funcId2 + "," +
                        "\"analytical_function_id\": " + funcId2 + "," +
                        "\"x_from\": 0.0," +
                        "\"x_to\": 10.0," +
                        "\"count\": 5" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/functions/" + funcId2 + "/sampling", request, String.class);

                return getStatusFromResponse(response);
            });

            // 19. Операция сложения
            timeMeasurement("Perform_Operation_Add", () -> {
                String requestJson = "{" +
                        "\"function1_id\": " + funcId1 + "," +
                        "\"function2_id\": " + funcId2 + "," +
                        "\"operation\": \"add\"" +
                        "}";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

                ResponseEntity<String> response = publicRestTemplate.postForEntity(
                        baseUrl + "/operation", request, String.class);

                return getStatusFromResponse(response);
            });

            // 20. Операция дифференцирования
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

            // 21. Удаление функции
            timeMeasurement("Delete_Function", () -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", basicAuthHeader[0]);
                HttpEntity<String> request = new HttpEntity<>(headers);

                ResponseEntity<String> response = publicRestTemplate.exchange(
                        baseUrl + "/functions/" + funcId2,
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
                int idIndex = responseBody.indexOf("\"id\":");
                if (idIndex != -1) {
                    int start = idIndex + 5;
                    int end = responseBody.indexOf(",", start);
                    if (end == -1) end = responseBody.indexOf("}", start);
                    String idStr = responseBody.substring(start, end).trim();
                    return Long.parseLong(idStr);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse ID from response: " + responseBody);
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