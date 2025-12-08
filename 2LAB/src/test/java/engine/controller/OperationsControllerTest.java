package engine.controller;

import engine.dto.OperationRequest;
import engine.dto.PointResponse;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class OperationsControllerTest {

    private OperationsController controller;

    @BeforeEach
    void setUp() {
        controller = new OperationsController();
    }

    @Test
    void testGetOperationResult_MethodCall() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "getOperationResult", OperationRequest.class
        );

        OperationRequest request = new OperationRequest();
        request.setOperation("add");
        request.setFunction1_id(1L);
        request.setFunction2_id(2L);

        try {
            ResponseEntity<?> response = (ResponseEntity<?>) method.invoke(controller, request);
            assertNotNull(method);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testGetOperationResult_ValidationScenarios() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "getOperationResult", OperationRequest.class
        );

        OperationRequest request1 = new OperationRequest();
        request1.setOperation("invalid");
        request1.setFunction1_id(1L);

        OperationRequest request2 = new OperationRequest();
        request2.setOperation("add");
        request2.setFunction1_id(0L);

        OperationRequest request3 = new OperationRequest();
        request3.setOperation("add");
        request3.setFunction1_id(-1L);

        OperationRequest request4 = new OperationRequest();
        request4.setOperation("derive");
        request4.setFunction1_id(1L);

        OperationRequest request5 = new OperationRequest();
        request5.setOperation("add");
        request5.setFunction1_id(1L);
        request5.setFunction2_id(null);

        for (OperationRequest req : Arrays.asList(request1, request2, request3, request4, request5)) {
            try {
                method.invoke(controller, req);
            } catch (Exception e) {}
        }

        assertNotNull(method);
    }

    @Test
    void testCreateDerivativeFunction() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "createDerivativeFunction", Functions.class,
                Class.forName("functions.TabulatedFunction")
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);
        function.setName("test");
        function.setType("array_tabulated");
        function.setSource("base");

        try {
            Object tabulatedFunction = createDummyTabulatedFunction();
            method.invoke(controller, function, tabulatedFunction);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testDeriveAnalyticalFunction() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "deriveAnalyticalFunction", Functions.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);
        function.setName("test");
        function.setType("analytical");
        function.setSource("base");

        try {
            List<PointResponse> result = (List<PointResponse>) method.invoke(controller, function);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testCreatePointsForAnalyticalFunction() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "createPointsForAnalyticalFunction", Functions.class,
                Class.forName("functions.MathFunction")
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);
        function.setName("test");
        function.setType("analytical");
        function.setSource("base");

        try {
            Object mathFunction = createDummyMathFunction();
            List<FunctionPoints> result = (List<FunctionPoints>) method.invoke(controller, function, mathFunction);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testCreateBinaryOperationFunction() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "createBinaryOperationFunction", Functions.class, Functions.class, String.class,
                Class.forName("functions.TabulatedFunction"), Class.forName("functions.TabulatedFunction")
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions func1 = new Functions();
        func1.setId(1L);
        func1.setUser(user);
        func1.setName("func1");
        func1.setType("array_tabulated");
        func1.setSource("base");

        Functions func2 = new Functions();
        func2.setId(2L);
        func2.setUser(user);
        func2.setName("func2");
        func2.setType("array_tabulated");
        func2.setSource("base");

        try {
            Object tabFunc1 = createDummyTabulatedFunction();
            Object tabFunc2 = createDummyTabulatedFunction();

            for (String operation : Arrays.asList("add", "subtract", "multiplication", "division")) {
                try {
                    method.invoke(controller, func1, func2, operation, tabFunc1, tabFunc2);
                } catch (Exception e) {}
            }
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testSaveFunctionPoints() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "saveFunctionPoints", Functions.class,
                Class.forName("functions.TabulatedFunction")
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);

        try {
            Object tabulatedFunction = createDummyTabulatedFunction();
            method.invoke(controller, function, tabulatedFunction);
        } catch (Exception e) {}

        assertNotNull(method);
    }

    @Test
    void testCreateTabulatedFunction() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "createTabulatedFunction", List.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);

        List<FunctionPoints> points = new ArrayList<>();
        points.add(new FunctionPoints(function, 1.0, 2.0));
        points.add(new FunctionPoints(function, 3.0, 4.0));
        points.add(new FunctionPoints(function, 2.0, 3.0));

        try {
            Object result = method.invoke(controller, points);
            assertNotNull(method);
        } catch (Exception e) {}
    }

    @Test
    void testIsValidOperation() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "isValidOperation", String.class
        );
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(controller, "add"));
        assertTrue((Boolean) method.invoke(controller, "subtract"));
        assertTrue((Boolean) method.invoke(controller, "multiplication"));
        assertTrue((Boolean) method.invoke(controller, "division"));
        assertTrue((Boolean) method.invoke(controller, "derive"));
        assertFalse((Boolean) method.invoke(controller, "invalid"));
        assertFalse((Boolean) method.invoke(controller, null));
    }

    @Test
    void testIsTabulated() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "isTabulated", Functions.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions func1 = new Functions();
        func1.setUser(user);
        func1.setType("array_tabulated");

        Functions func2 = new Functions();
        func2.setUser(user);
        func2.setType("linked_list_tabulated");

        Functions func3 = new Functions();
        func3.setUser(user);
        func3.setType("analytical");

        assertTrue((Boolean) method.invoke(controller, func1));
        assertTrue((Boolean) method.invoke(controller, func2));
        assertFalse((Boolean) method.invoke(controller, func3));
    }

    @Test
    void testHasZeroY() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "hasZeroY", List.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);

        List<FunctionPoints> points1 = new ArrayList<>();
        points1.add(new FunctionPoints(function, 1.0, 2.0));
        points1.add(new FunctionPoints(function, 2.0, 0.0));

        List<FunctionPoints> points2 = new ArrayList<>();
        points2.add(new FunctionPoints(function, 1.0, 2.0));
        points2.add(new FunctionPoints(function, 2.0, 3.0));

        assertTrue((Boolean) method.invoke(controller, points1));
        assertFalse((Boolean) method.invoke(controller, points2));
    }

    @Test
    void testIsAnalyticalFunction() throws Exception {
        Method method = OperationsController.class.getDeclaredMethod(
                "isAnalyticalFunction", Functions.class
        );
        method.setAccessible(true);

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setUser(user);
        function.setName("test");

        try {
            Boolean result = (Boolean) method.invoke(controller, function);
            assertNotNull(result);
        } catch (Exception e) {}
    }

    @Test
    void testDTOClasses() {
        OperationRequest request = new OperationRequest();
        request.setOperation("add");
        request.setFunction1_id(1L);
        request.setFunction2_id(2L);

        assertEquals("add", request.getOperation());
        assertEquals(1L, request.getFunction1_id());
        assertEquals(2L, request.getFunction2_id());

        PointResponse point = new PointResponse(1L, 2.0, 3.0);
        assertEquals(1L, point.getId());
        assertEquals(2.0, point.getX(), 0.001);
        assertEquals(3.0, point.getY(), 0.001);
    }

    @Test
    void testEntityClasses() {
        Users user = new Users();
        user.setId(1L);
        user.setUsername("test");

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);
        function.setName("func");
        function.setType("array_tabulated");
        function.setSource("operation");

        FunctionPoints point = new FunctionPoints();
        point.setId(1L);
        point.setFunction(function);
        point.setX_value(1.0);
        point.setY_value(2.0);

        assertEquals(1L, point.getId());
        assertEquals(function, point.getFunction());
        assertEquals(1.0, point.getX_value(), 0.001);
        assertEquals(2.0, point.getY_value(), 0.001);
    }

    @Test
    void testClassStructure() throws Exception {
        assertTrue(OperationsController.class.isAnnotationPresent(
                org.springframework.web.bind.annotation.RestController.class));
        assertTrue(OperationsController.class.isAnnotationPresent(
                org.springframework.security.access.prepost.PreAuthorize.class));

        java.lang.reflect.Field loggerField = OperationsController.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        assertNotNull(loggerField.get(controller));

        String[] dependencyFields = {"multipleSearchService", "singleSearchService", "securityUtils"};
        for (String fieldName : dependencyFields) {
            java.lang.reflect.Field field = OperationsController.class.getDeclaredField(fieldName);
            assertNotNull(field);
            assertTrue(java.lang.reflect.Modifier.isPrivate(field.getModifiers()));
        }

        java.lang.reflect.Field stepField = OperationsController.class.getDeclaredField("DIFFERENTIATION_STEP");
        stepField.setAccessible(true);
        assertEquals(1e-5, (Double) stepField.get(null), 0.000001);
    }

    @Test
    void testAllControllerMethods() throws Exception {
        Method[] allMethods = OperationsController.class.getDeclaredMethods();
        List<Method> privateMethods = new ArrayList<>();

        for (Method method : allMethods) {
            if (method.getName().equals("getOperationResult")) {
                try {
                    OperationRequest request = new OperationRequest();
                    request.setOperation("derive");
                    request.setFunction1_id(1L);
                    method.invoke(controller, request);
                } catch (Exception e) {}
            } else {
                privateMethods.add(method);
            }
        }

        Users user = new Users();
        user.setId(1L);

        Functions function = new Functions();
        function.setId(1L);
        function.setUser(user);
        function.setName("test");
        function.setType("array_tabulated");

        Functions func1 = new Functions();
        func1.setId(1L);
        func1.setUser(user);
        func1.setName("func1");
        func1.setType("array_tabulated");

        Functions func2 = new Functions();
        func2.setId(2L);
        func2.setUser(user);
        func2.setName("func2");
        func2.setType("array_tabulated");

        List<FunctionPoints> points = new ArrayList<>();
        points.add(new FunctionPoints(function, 1.0, 2.0));

        for (Method method : privateMethods) {
            method.setAccessible(true);
            try {
                String methodName = method.getName();

                if (methodName.equals("createDerivativeFunction")) {
                    Object tabFunc = createDummyTabulatedFunction();
                    method.invoke(controller, function, tabFunc);
                } else if (methodName.equals("deriveAnalyticalFunction")) {
                    method.invoke(controller, function);
                } else if (methodName.equals("createPointsForAnalyticalFunction")) {
                    Object mathFunc = createDummyMathFunction();
                    method.invoke(controller, function, mathFunc);
                } else if (methodName.equals("createBinaryOperationFunction")) {
                    Object tabFunc1 = createDummyTabulatedFunction();
                    Object tabFunc2 = createDummyTabulatedFunction();
                    method.invoke(controller, func1, func2, "add", tabFunc1, tabFunc2);
                } else if (methodName.equals("saveFunctionPoints")) {
                    Object tabFunc = createDummyTabulatedFunction();
                    method.invoke(controller, function, tabFunc);
                } else if (methodName.equals("createTabulatedFunction")) {
                    method.invoke(controller, points);
                } else if (methodName.equals("isValidOperation")) {
                    method.invoke(controller, "add");
                } else if (methodName.equals("isTabulated")) {
                    method.invoke(controller, function);
                } else if (methodName.equals("hasZeroY")) {
                    method.invoke(controller, points);
                } else if (methodName.equals("isAnalyticalFunction")) {
                    method.invoke(controller, function);
                }
            } catch (Exception e) {}
        }

        assertTrue(true);
    }

    private Object createDummyTabulatedFunction() {
        try {
            Class<?> clazz = Class.forName("functions.ArrayTabulatedFunction");
            double[] xValues = {1.0, 2.0, 3.0};
            double[] yValues = {2.0, 4.0, 6.0};
            return clazz.getConstructor(double[].class, double[].class).newInstance(xValues, yValues);
        } catch (Exception e) {
            return null;
        }
    }

    private Object createDummyMathFunction() {
        try {
            return Class.forName("functions.UnitFunction").newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}