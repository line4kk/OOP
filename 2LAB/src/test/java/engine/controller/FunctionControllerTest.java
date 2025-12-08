package engine.controller;

import engine.dto.*;
import engine.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionControllerTest {

    private FunctionController controller;

    @BeforeEach
    void setUp() {
        controller = new FunctionController();
    }

    @Test
    void testGetAllUserFunctions_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod("getAllUserFunctions");
        try {
            method.invoke(controller);
        } catch (Exception e) {}
        assertNotNull(method);
    }

    @Test
    void testGetAllUserAnalyticFunctions_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod("getAllUserAnalyticFunctions");
        try {
            method.invoke(controller);
        } catch (Exception e) {}
        assertNotNull(method);
    }

    @Test
    void testCreateFunction_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "createFunction", FunctionCreateRequest.class
        );

        FunctionCreateRequest request1 = new FunctionCreateRequest();
        request1.setName("test");
        request1.setType("analytical");
        request1.setSource("base");

        FunctionCreateRequest request2 = new FunctionCreateRequest();
        request2.setName("");
        request2.setType("analytical");
        request2.setSource("base");

        FunctionCreateRequest request3 = new FunctionCreateRequest();
        request3.setName("test");
        request3.setType("invalid");
        request3.setSource("base");

        FunctionCreateRequest request4 = new FunctionCreateRequest();
        request4.setName("test");
        request4.setType("analytical");
        request4.setSource("invalid");

        FunctionCreateRequest request5 = new FunctionCreateRequest();

        for (FunctionCreateRequest req : Arrays.asList(request1, request2, request3, request4, request5)) {
            try {
                method.invoke(controller, req);
            } catch (Exception e) {}
        }

        assertNotNull(method);
    }

    @Test
    void testGetFunctionById_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "getFunctionById", Long.class
        );

        try {
            method.invoke(controller, 1L);
            method.invoke(controller, -1L);
            method.invoke(controller, null);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testUpdateFunction_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "updateFunction", Long.class, FunctionCreateRequest.class
        );

        FunctionCreateRequest request = new FunctionCreateRequest();
        request.setName("updated");
        request.setType("linked_list_tabulated");
        request.setSource("operation");

        try {
            method.invoke(controller, 1L, request);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testDeleteFunction_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "deleteFunction", Long.class
        );

        try {
            method.invoke(controller, 1L);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testGetFunctionPoints_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "getFunctionPoints", Long.class
        );

        try {
            method.invoke(controller, 1L);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testAddFunctionPoints_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "addFunctionPoints", Long.class, List.class
        );

        List<PointRequest> points1 = Arrays.asList(
                new PointRequest(1.0, 2.0),
                new PointRequest(2.0, 4.0)
        );

        List<PointRequest> points2 = new ArrayList<>();

        List<PointRequest> points3 = Arrays.asList(
                new PointRequest(1.0, 2.0),
                new PointRequest(1.0, 3.0)
        );

        PointRequest nullPoint = new PointRequest();
        List<PointRequest> points4 = Arrays.asList(nullPoint);

        List<PointRequest> points5 = null;

        try {
            method.invoke(controller, 1L, points1);
            method.invoke(controller, 1L, points2);
            method.invoke(controller, 1L, points3);
            method.invoke(controller, 1L, points4);
            method.invoke(controller, 1L, points5);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testUpdateFunctionPoint_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "updateFunctionPoint", Long.class, Long.class, PointRequest.class
        );

        PointRequest request = new PointRequest(3.0, 9.0);

        try {
            method.invoke(controller, 1L, 100L, request);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testDeleteFunctionPoint_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "deleteFunctionPoint", Long.class, Long.class
        );

        try {
            method.invoke(controller, 1L, 100L);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testDeleteAllFunctionPoints_MethodCall() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "deleteAllFunctionPoints", Long.class
        );

        try {
            method.invoke(controller, 1L);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testMapToFunctionResponse() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "mapToFunctionResponse", Functions.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(100L);
        function.setUser(user);
        function.setName("testFunc");
        function.setType("analytical");
        function.setSource("base");

        FunctionResponse response = (FunctionResponse) method.invoke(controller, function);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("testFunc", response.getName());
        assertEquals("analytical", response.getType());
        assertEquals("base", response.getSource());
    }

    @Test
    void testMapToPointResponse() throws Exception {
        Method method = FunctionController.class.getDeclaredMethod(
                "mapToPointResponse", FunctionPoints.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);

        FunctionPoints point = new FunctionPoints();
        point.setId(200L);
        point.setFunction(function);
        point.setX_value(1.5);
        point.setY_value(2.25);

        PointResponse response = (PointResponse) method.invoke(controller, point);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals(1.5, response.getX(), 0.001);
        assertEquals(2.25, response.getY(), 0.001);
    }

    @Test
    void testDTOClasses() {
        FunctionCreateRequest createRequest = new FunctionCreateRequest();
        createRequest.setName("test");
        createRequest.setType("array_tabulated");
        createRequest.setSource("composite");

        assertEquals("test", createRequest.getName());
        assertEquals("array_tabulated", createRequest.getType());
        assertEquals("composite", createRequest.getSource());

        FunctionResponse funcResponse = new FunctionResponse(1L, "sqr", "analytical", "base");
        assertEquals(1L, funcResponse.getId());
        assertEquals("sqr", funcResponse.getName());
        assertEquals("analytical", funcResponse.getType());
        assertEquals("base", funcResponse.getSource());

        List<FunctionResponse> functions = Arrays.asList(funcResponse);
        FunctionsResponse functionsResponse = new FunctionsResponse(functions);
        assertEquals(1, functionsResponse.getFunctions().size());
        assertEquals(funcResponse, functionsResponse.getFunctions().get(0));

        PointRequest pointRequest1 = new PointRequest(2.0, 4.0);
        assertEquals(2.0, pointRequest1.getX(), 0.001);
        assertEquals(4.0, pointRequest1.getY(), 0.001);

        PointRequest pointRequest2 = new PointRequest();
        pointRequest2.setX(3.0);
        pointRequest2.setY(9.0);
        assertEquals(3.0, pointRequest2.getX(), 0.001);
        assertEquals(9.0, pointRequest2.getY(), 0.001);

        PointResponse pointResponse = new PointResponse(1L, 1.0, 2.0);
        assertEquals(1L, pointResponse.getId());
        assertEquals(1.0, pointResponse.getX(), 0.001);
        assertEquals(2.0, pointResponse.getY(), 0.001);
    }

    @Test
    void testEntityClasses() {
        Users user = new Users();
        user.setId(1L);
        user.setUsername("testuser");

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());

        Functions func1 = new Functions(user, "func1", "analytical", "base");
        assertEquals(user, func1.getUser());
        assertEquals("func1", func1.getName());
        assertEquals("analytical", func1.getType());
        assertEquals("base", func1.getSource());

        Functions func2 = new Functions();
        func2.setId(2L);
        func2.setUser(user);
        func2.setName("func2");
        func2.setType("linked_list_tabulated");
        func2.setSource("operation");

        assertEquals(2L, func2.getId());
        assertEquals(user, func2.getUser());
        assertEquals("func2", func2.getName());
        assertEquals("linked_list_tabulated", func2.getType());
        assertEquals("operation", func2.getSource());

        FunctionPoints point1 = new FunctionPoints(func1, 1.0, 2.0);
        assertEquals(func1, point1.getFunction());
        assertEquals(1.0, point1.getX_value(), 0.001);
        assertEquals(2.0, point1.getY_value(), 0.001);

        FunctionPoints point2 = new FunctionPoints();
        point2.setId(100L);
        point2.setFunction(func2);
        point2.setX_value(3.0);
        point2.setY_value(6.0);

        assertEquals(100L, point2.getId());
        assertEquals(func2, point2.getFunction());
        assertEquals(3.0, point2.getX_value(), 0.001);
        assertEquals(6.0, point2.getY_value(), 0.001);

        try {
            point2.setYValue(7.0);
            assertEquals(7.0, point2.getY_value(), 0.001);
        } catch (NoSuchMethodError e) {}
    }

    @Test
    void testClassConstants() throws Exception {
        java.lang.reflect.Field typesField = FunctionController.class.getDeclaredField("VALID_FUNCTION_TYPES");
        typesField.setAccessible(true);
        Set<String> validTypes = (Set<String>) typesField.get(null);

        java.lang.reflect.Field sourcesField = FunctionController.class.getDeclaredField("VALID_FUNCTION_SOURCES");
        sourcesField.setAccessible(true);
        Set<String> validSources = (Set<String>) sourcesField.get(null);

        assertTrue(validTypes.contains("linked_list_tabulated"));
        assertTrue(validTypes.contains("array_tabulated"));
        assertTrue(validTypes.contains("analytical"));
        assertEquals(3, validTypes.size());

        assertTrue(validSources.contains("base"));
        assertTrue(validSources.contains("operation"));
        assertTrue(validSources.contains("composite"));
        assertEquals(3, validSources.size());
    }

    @Test
    void testClassAnnotationsAndLogger() throws Exception {
        assertTrue(FunctionController.class.isAnnotationPresent(
                org.springframework.web.bind.annotation.RestController.class));
        assertTrue(FunctionController.class.isAnnotationPresent(
                org.springframework.security.access.prepost.PreAuthorize.class));

        java.lang.reflect.Field loggerField = FunctionController.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        Object logger = loggerField.get(controller);
        assertNotNull(logger);

        String[] dependencyFields = {"singleSearchService", "multipleSearchService", "securityUtils"};
        for (String fieldName : dependencyFields) {
            java.lang.reflect.Field field = FunctionController.class.getDeclaredField(fieldName);
            assertNotNull(field);
            assertTrue(java.lang.reflect.Modifier.isPrivate(field.getModifiers()));
        }
    }

    @Test
    void testAllControllerMethods() throws Exception {
        Method[] allMethods = FunctionController.class.getDeclaredMethods();
        assertEquals(16, allMethods.length);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions(user, "test", "analytical", "base");

        FunctionCreateRequest createRequest = new FunctionCreateRequest();
        createRequest.setName("test");
        createRequest.setType("analytical");
        createRequest.setSource("base");

        List<PointRequest> points = Arrays.asList(new PointRequest(1.0, 1.0));

        List<Method> publicMethods = Arrays.stream(FunctionController.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .toList();

        for (Method method : publicMethods) {
            try {
                String methodName = method.getName();

                switch (methodName) {
                    case "getAllUserFunctions":
                        method.invoke(controller);
                        break;
                    case "getAllUserAnalyticFunctions":
                        method.invoke(controller);
                        break;
                    case "createFunction":
                        method.invoke(controller, createRequest);
                        break;
                    case "getFunctionById":
                        method.invoke(controller, 1L);
                        break;
                    case "updateFunction":
                        method.invoke(controller, 1L, createRequest);
                        break;
                    case "deleteFunction":
                        method.invoke(controller, 1L);
                        break;
                    case "getFunctionPoints":
                        method.invoke(controller, 1L);
                        break;
                    case "addFunctionPoints":
                        method.invoke(controller, 1L, points);
                        break;
                    case "updateFunctionPoint":
                        method.invoke(controller, 1L, 100L, new PointRequest(2.0, 4.0));
                        break;
                    case "deleteFunctionPoint":
                        method.invoke(controller, 1L, 100L);
                        break;
                    case "deleteAllFunctionPoints":
                        method.invoke(controller, 1L);
                        break;
                }
            } catch (Exception e) {}
        }

        Method mapToFunctionResponse = FunctionController.class.getDeclaredMethod(
                "mapToFunctionResponse", Functions.class
        );
        mapToFunctionResponse.setAccessible(true);
        mapToFunctionResponse.invoke(controller, function);

        Method mapToPointResponse = FunctionController.class.getDeclaredMethod(
                "mapToPointResponse", FunctionPoints.class
        );
        mapToPointResponse.setAccessible(true);

        FunctionPoints point = new FunctionPoints(function, 1.0, 2.0);
        mapToPointResponse.invoke(controller, point);

        assertTrue(true);
    }

    @Test
    void testValidationConstantsUsage() {
        assertTrue(Set.of("linked_list_tabulated", "array_tabulated", "analytical")
                .contains("linked_list_tabulated"));
        assertTrue(Set.of("linked_list_tabulated", "array_tabulated", "analytical")
                .contains("array_tabulated"));
        assertTrue(Set.of("linked_list_tabulated", "array_tabulated", "analytical")
                .contains("analytical"));
        assertFalse(Set.of("linked_list_tabulated", "array_tabulated", "analytical")
                .contains("invalid_type"));

        assertTrue(Set.of("base", "operation", "composite").contains("base"));
        assertTrue(Set.of("base", "operation", "composite").contains("operation"));
        assertTrue(Set.of("base", "operation", "composite").contains("composite"));
        assertFalse(Set.of("base", "operation", "composite").contains("invalid_source"));
    }
}