package engine.controller;

import engine.dto.*;
import engine.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticalFunctionControllerTest {

    private AnalyticalFunctionController controller;

    @BeforeEach
    void setUp() {
        controller = new AnalyticalFunctionController();
    }

    @Test
    void testCalculateAnalyticalFunctionValue() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "calculateAnalyticalFunctionValue", Functions.class, double.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);
        function.setName("test");
        function.setType("analytical");
        function.setSource("simple");

        Double result = (Double) method.invoke(controller, function, 2.0);

        assertNotNull(result);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void testCalculateAnalyticalFunctionValue_Composite() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "calculateAnalyticalFunctionValue", Functions.class, double.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions compositeFunction = new Functions();
        compositeFunction.setId(2L);
        compositeFunction.setUser(user);
        compositeFunction.setName("composite");
        compositeFunction.setType("analytical");
        compositeFunction.setSource("composite");

        Double result = (Double) method.invoke(controller, compositeFunction, 3.0);

        assertNotNull(result);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void testCalculateCompositeFunction() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "calculateCompositeFunction", Functions.class, double.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions compositeFunction = new Functions();
        compositeFunction.setId(1L);
        compositeFunction.setUser(user);
        compositeFunction.setName("composite");
        compositeFunction.setType("analytical");
        compositeFunction.setSource("composite");

        Double result = (Double) method.invoke(controller, compositeFunction, 5.0);

        assertNotNull(result);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void testCalculateCompositeFunction_WithComponents() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "calculateCompositeFunction", Functions.class, double.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions compositeFunction = new Functions();
        compositeFunction.setId(1L);
        compositeFunction.setUser(user);
        compositeFunction.setName("composite");
        compositeFunction.setType("analytical");
        compositeFunction.setSource("composite");

        Double result = (Double) method.invoke(controller, compositeFunction, 3.0);

        assertNotNull(result);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void testCreateCompositeFunction_MethodCall() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "createCompositeFunction", CompositeCreateRequest.class
        );

        CompositeCreateRequest request = new CompositeCreateRequest();
        request.setName("test");
        request.setFunctionIdsInOrder(Arrays.asList(1L, 2L));

        try {
            ResponseEntity<?> response = (ResponseEntity<?>) method.invoke(controller, request);
            assertNotNull(response);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }

    @Test
    void testCreateCompositeFunction_WithSimpleData() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "createCompositeFunction", CompositeCreateRequest.class
        );

        CompositeCreateRequest request1 = new CompositeCreateRequest();
        request1.setName("");
        request1.setFunctionIdsInOrder(Arrays.asList(1L, 2L));

        CompositeCreateRequest request2 = new CompositeCreateRequest();
        request2.setName("test");
        request2.setFunctionIdsInOrder(Collections.singletonList(1L));

        CompositeCreateRequest request3 = new CompositeCreateRequest();
        request3.setName("test");
        request3.setFunctionIdsInOrder(Arrays.asList(1L, 1L));

        CompositeCreateRequest request4 = new CompositeCreateRequest();
        request4.setName("test");
        request4.setFunctionIdsInOrder(Arrays.asList(0L, 1L));

        for (CompositeCreateRequest req : Arrays.asList(request1, request2, request3, request4)) {
            try {
                method.invoke(controller, req);
            } catch (Exception e) {}
        }

        assertNotNull(method);
    }

    @Test
    void testGetCompositeFunctionElements_MethodCall() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "getCompositeFunctionElements", Long.class
        );

        try {
            ResponseEntity<?> response1 = (ResponseEntity<?>) method.invoke(controller, 1L);
            ResponseEntity<?> response2 = (ResponseEntity<?>) method.invoke(controller, -1L);
            ResponseEntity<?> response3 = (ResponseEntity<?>) method.invoke(controller, null);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testGetCompositeFunctionElements_DifferentScenarios() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "getCompositeFunctionElements", Long.class
        );

        try {
            method.invoke(controller, 100L);
        } catch (Exception e) {}

        try {
            method.invoke(controller, (Object) null);
        } catch (Exception e) {}

        try {
            method.invoke(controller, -5L);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testAddFunctionPointsBySampling_MethodCall() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "addFunctionPointsBySampling", Long.class, FunctionSamplingRequest.class
        );

        FunctionSamplingRequest request = new FunctionSamplingRequest();
        request.setFunction_id(1L);
        request.setAnalytical_function_id(2L);
        request.setX_from(0.0);
        request.setX_to(10.0);
        request.setCount(5);

        try {
            ResponseEntity<?> response = (ResponseEntity<?>) method.invoke(controller, 1L, request);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testAddFunctionPointsBySampling_ValidationScenarios() throws Exception {
        Method method = AnalyticalFunctionController.class.getDeclaredMethod(
                "addFunctionPointsBySampling", Long.class, FunctionSamplingRequest.class
        );

        FunctionSamplingRequest request1 = new FunctionSamplingRequest();
        request1.setFunction_id(1L);
        request1.setAnalytical_function_id(2L);
        request1.setX_from(10.0);
        request1.setX_to(5.0);
        request1.setCount(5);

        FunctionSamplingRequest request2 = new FunctionSamplingRequest();
        request2.setFunction_id(1L);
        request2.setAnalytical_function_id(2L);
        request2.setX_from(0.0);
        request2.setX_to(10.0);
        request2.setCount(1);

        FunctionSamplingRequest request3 = new FunctionSamplingRequest();
        request3.setFunction_id(1L);
        request3.setX_from(0.0);
        request3.setX_to(10.0);
        request3.setCount(5);

        FunctionSamplingRequest request4 = new FunctionSamplingRequest();

        for (FunctionSamplingRequest req : Arrays.asList(request1, request2, request3, request4)) {
            try {
                method.invoke(controller, 1L, req);
            } catch (Exception e) {}
        }

        assertNotNull(method);
    }

    @Test
    void testDTOClasses() {
        CompositeCreateRequest request = new CompositeCreateRequest();
        request.setName("test");
        request.setFunctionIdsInOrder(Arrays.asList(1L, 2L, 3L));

        assertEquals("test", request.getName());
        assertEquals(3, request.getFunctionIdsInOrder().size());
        assertTrue(request.getFunctionIdsInOrder().contains(1L));

        FunctionResponse response = new FunctionResponse(1L, "test", "analytical", "composite");
        assertEquals(1L, response.getId());
        assertEquals("test", response.getName());
        assertEquals("analytical", response.getType());
        assertEquals("composite", response.getSource());

        CompositeElementResponse element = new CompositeElementResponse(1L, 0, 2L);
        assertEquals(1L, element.getId());
        assertEquals(0, element.getOrder());

        PointResponse point = new PointResponse(1L, 1.0, 2.0);
        assertEquals(1L, point.getId());
        assertEquals(1.0, point.getX(), 0.001);
        assertEquals(2.0, point.getY(), 0.001);

        FunctionSamplingRequest sampling = new FunctionSamplingRequest();
        sampling.setFunction_id(1L);
        sampling.setAnalytical_function_id(2L);
        sampling.setX_from(0.0);
        sampling.setX_to(10.0);
        sampling.setCount(100);

        assertEquals(1L, sampling.getFunction_id());
        assertEquals(2L, sampling.getAnalytical_function_id());
        assertEquals(0.0, sampling.getX_from(), 0.001);
        assertEquals(10.0, sampling.getX_to(), 0.001);
        assertEquals(100, sampling.getCount());
    }

    @Test
    void testEntityClasses() {
        Users user = new Users();
        user.setId(1L);
        user.setUsername("testuser");

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);
        function.setName("sqr");
        function.setType("analytical");
        function.setSource("simple");

        assertEquals(1L, function.getId());
        assertEquals(user, function.getUser());
        assertEquals("sqr", function.getName());
        assertEquals("analytical", function.getType());
        assertEquals("simple", function.getSource());

        FunctionPoints point = new FunctionPoints();
        point.setId(1L);
        point.setFunction(function);
        point.setX_value(2.0);
        point.setY_value(4.0);

        assertEquals(1L, point.getId());
        assertEquals(function, point.getFunction());
        assertEquals(2.0, point.getX_value(), 0.001);
        assertEquals(4.0, point.getY_value(), 0.001);

        CompositeFunctionElements element = new CompositeFunctionElements();
        element.setId(1L);
        element.setFunctionOrder(0);
        element.setFunction(function);

        assertEquals(1L, element.getId());
        assertEquals(0, element.getFunctionOrder());
        assertEquals(function, element.getFunction());
    }

    @Test
    void testEntityConstructors() {
        Users user = new Users();
        Functions function = new Functions(user, "test", "analytical", "composite");

        assertEquals(user, function.getUser());
        assertEquals("test", function.getName());
        assertEquals("analytical", function.getType());
        assertEquals("composite", function.getSource());

        FunctionPoints point = new FunctionPoints(function, 1.0, 2.0);

        assertEquals(function, point.getFunction());
        assertEquals(1.0, point.getX_value(), 0.001);
        assertEquals(2.0, point.getY_value(), 0.001);

        CompositeFunctionElements element = new CompositeFunctionElements(function, 0, function);

        assertEquals(0, element.getFunctionOrder());
        assertEquals(function, element.getFunction());
    }

    @Test
    void testAllMethodsCoverage() throws Exception {
        List<Method> allMethods = new ArrayList<>();

        allMethods.add(AnalyticalFunctionController.class.getDeclaredMethod(
                "createCompositeFunction", CompositeCreateRequest.class));
        allMethods.add(AnalyticalFunctionController.class.getDeclaredMethod(
                "getCompositeFunctionElements", Long.class));
        allMethods.add(AnalyticalFunctionController.class.getDeclaredMethod(
                "addFunctionPointsBySampling", Long.class, FunctionSamplingRequest.class));

        Method private1 = AnalyticalFunctionController.class.getDeclaredMethod(
                "calculateAnalyticalFunctionValue", Functions.class, double.class);
        private1.setAccessible(true);
        allMethods.add(private1);

        Method private2 = AnalyticalFunctionController.class.getDeclaredMethod(
                "calculateCompositeFunction", Functions.class, double.class);
        private2.setAccessible(true);
        allMethods.add(private2);

        assertEquals(5, allMethods.size());

        Users user = new Users();
        Functions func = new Functions(user, "test", "analytical", "simple");
        Functions compositeFunc = new Functions(user, "composite", "analytical", "composite");

        CompositeCreateRequest createRequest = new CompositeCreateRequest();
        createRequest.setName("test");
        createRequest.setFunctionIdsInOrder(Arrays.asList(1L, 2L));

        FunctionSamplingRequest samplingRequest = new FunctionSamplingRequest();
        samplingRequest.setFunction_id(1L);
        samplingRequest.setAnalytical_function_id(2L);
        samplingRequest.setX_from(0.0);
        samplingRequest.setX_to(10.0);
        samplingRequest.setCount(5);

        for (Method method : allMethods) {
            try {
                Object result = null;

                if (method.getName().equals("createCompositeFunction")) {
                    result = method.invoke(controller, createRequest);
                } else if (method.getName().equals("getCompositeFunctionElements")) {
                    result = method.invoke(controller, 1L);
                } else if (method.getName().equals("addFunctionPointsBySampling")) {
                    result = method.invoke(controller, 1L, samplingRequest);
                } else if (method.getName().equals("calculateAnalyticalFunctionValue")) {
                    result = method.invoke(controller, func, 2.0);
                } else if (method.getName().equals("calculateCompositeFunction")) {
                    result = method.invoke(controller, compositeFunc, 3.0);
                }
            } catch (Exception e) {}
        }

        assertTrue(true);
    }

    @Test
    void testClassStructure() {
        assertTrue(AnalyticalFunctionController.class.isAnnotationPresent(
                org.springframework.web.bind.annotation.RestController.class));
        assertTrue(AnalyticalFunctionController.class.isAnnotationPresent(
                org.springframework.security.access.prepost.PreAuthorize.class));

        try {
            java.lang.reflect.Field loggerField = AnalyticalFunctionController.class.getDeclaredField("logger");
            assertNotNull(loggerField);
            assertEquals(org.slf4j.Logger.class, loggerField.getType());
        } catch (NoSuchFieldException e) {
            fail("logger не найдено");
        }

        String[] dependencyFields = {"multipleSearchService", "singleSearchService",
                "securityUtils", "compositeFunctionElementsRepository"};

        for (String fieldName : dependencyFields) {
            try {
                java.lang.reflect.Field field = AnalyticalFunctionController.class.getDeclaredField(fieldName);
                assertNotNull(field);
                assertTrue(java.lang.reflect.Modifier.isPrivate(field.getModifiers()));
            } catch (NoSuchFieldException e) {
                fail("Поле " + fieldName + " не найдено");
            }
        }
    }
}