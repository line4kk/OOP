package engine.service;

import engine.entity.*;
import engine.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class SingleSearchServiceTest {

    private SingleSearchService service;
    private TestData testData;

    @BeforeEach
    void setUp() {
        testData = new TestData();

        service = new SingleSearchService();

        UsersRepository usersRepoProxy = createProxy(UsersRepository.class, testData.usersRepoHandler);
        FunctionsRepository functionsRepoProxy = createProxy(FunctionsRepository.class, testData.functionsRepoHandler);
        FunctionPointsRepository pointsRepoProxy = createProxy(FunctionPointsRepository.class, testData.pointsRepoHandler);
        CompositeFunctionElementsRepository compositeRepoProxy = createProxy(CompositeFunctionElementsRepository.class, testData.compositeRepoHandler);

        setField(service, "usersRepository", usersRepoProxy);
        setField(service, "functionsRepository", functionsRepoProxy);
        setField(service, "functionPointsRepository", pointsRepoProxy);
        setField(service, "compositeFunctionElementsRepository", compositeRepoProxy);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> interfaceType, TestInvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler
        );
    }

    @Test
    void testFindUserByUsername() {
        Users result = service.findUserByUsername("testUser");
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void testFindUserById() {
        Users result = service.findUserById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindUserByIdNotFound() {
        Users result = service.findUserById(999L);
        assertNull(result);
    }

    @Test
    void testFindFunctionById() {
        Optional<Functions> result = service.findFunctionById(1L);
        assertTrue(result.isPresent());
        assertEquals("testFunction", result.get().getName());
    }

    @Test
    void testFindFunctionByNameAndUser() {
        Users user = testData.testUser;
        Optional<Functions> result = service.findFunctionByNameAndUser("testFunction", user);
        assertTrue(result.isPresent());
    }

    @Test
    void testExistsFunctionByNameAndUser() {
        Users user = testData.testUser;
        assertTrue(service.existsFunctionByNameAndUser("testFunction", user));
        assertFalse(service.existsFunctionByNameAndUser("nonExistent", user));
    }

    @Test
    void testCountUsersByRole() {
        assertEquals(1, service.countUsersByRole("user"));
        assertEquals(0, service.countUsersByRole("admin"));
    }

    @Test
    void testSaveUser() {
        Users newUser = new Users(2L, "newUser", "hash", "admin", "array", new ArrayList<>());
        Users saved = service.saveUser(newUser);
        assertEquals("newUser", saved.getUsername());
    }

    @Test
    void testDeleteUser() {
        service.deleteUser(1L);
        Users result = service.findUserById(1L);
        assertNull(result);
    }

    @Test
    void testSaveFunction() {
        Functions newFunc = new Functions();
        newFunc.setId(2L);
        newFunc.setUser(testData.testUser);
        newFunc.setName("newFunc");

        Functions saved = service.saveFunction(newFunc);
        assertEquals("newFunc", saved.getName());
    }

    @Test
    void testDeleteFunction() {
        service.deleteFunction(1L);
        Optional<Functions> result = service.findFunctionById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testSaveFunctionPoint() {
        FunctionPoints newPoint = new FunctionPoints();
        newPoint.setId(2L);
        newPoint.setFunction(testData.testFunction);
        newPoint.setX_value(10.0);

        FunctionPoints saved = service.saveFunctionPoint(newPoint);
        assertEquals(10.0, saved.getX_value());
    }

    @Test
    void testFindFunctionPoint() {
        Optional<FunctionPoints> result = service.findFunctionPoint(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testFindFunctionPointByFunction() {
        Optional<FunctionPoints> result = service.findFunctionPointByFunction(1L, 1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testFindFunctionPointByX() {
        Optional<FunctionPoints> result = service.findFunctionPointByX(1L, 5.0);
        assertTrue(result.isPresent());
    }

    @Test
    void testDeleteFunctionPoint() {
        service.deleteFunctionPoint(testData.testPoint);
        Optional<FunctionPoints> result = service.findFunctionPoint(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testSaveCompositeFunctionElement() {
        CompositeFunctionElements element = new CompositeFunctionElements();
        element.setId(1L);
        element.setFunction(testData.testFunction);

        CompositeFunctionElements saved = service.saveCompositeFunctionElement(element);
        assertNotNull(saved);
    }

    @Test
    void testIsFunctionUsedInComposition() {
        assertFalse(service.isFunctionUsedInComposition(testData.testFunction));

        testData.addCompositeElement();

        assertTrue(service.isFunctionUsedInComposition(testData.testFunction));
    }

    static class TestData {
        Users testUser;
        Functions testFunction;
        FunctionPoints testPoint;

        TestInvocationHandler usersRepoHandler = new TestInvocationHandler();
        TestInvocationHandler functionsRepoHandler = new TestInvocationHandler();
        TestInvocationHandler pointsRepoHandler = new TestInvocationHandler();
        TestInvocationHandler compositeRepoHandler = new TestInvocationHandler();

        TestData() {
            testUser = new Users(1L, "testUser", "hash", "user", "array", new ArrayList<>());
            testFunction = new Functions();
            testFunction.setId(1L);
            testFunction.setUser(testUser);
            testFunction.setName("testFunction");

            testPoint = new FunctionPoints();
            testPoint.setId(1L);
            testPoint.setFunction(testFunction);
            testPoint.setX_value(5.0);

            setupUsersRepo();
            setupFunctionsRepo();
            setupPointsRepo();
            setupCompositeRepo();
        }

        void addCompositeElement() {
            CompositeFunctionElements element = new CompositeFunctionElements();
            element.setId(1L);
            element.setFunction(testFunction);

            compositeRepoHandler.methodHandlers.put("findByFunction",
                    (args) -> List.of(element));
        }

        private void setupUsersRepo() {
            Map<String, Function<Object[], Object>> handlers = usersRepoHandler.methodHandlers;
            Map<Long, Users> users = new HashMap<>();
            users.put(1L, testUser);

            handlers.put("findByUsername", args -> {
                String username = (String) args[0];
                return users.values().stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .orElse(null);
            });

            handlers.put("findById", args -> {
                Long id = (Long) args[0];
                return Optional.ofNullable(users.get(id));
            });

            handlers.put("countByRole", args -> {
                String role = (String) args[0];
                return (long) users.values().stream()
                        .filter(u -> u.getRole().equals(role))
                        .count();
            });

            handlers.put("save", args -> {
                Users user = (Users) args[0];
                users.put(user.getId(), user);
                return user;
            });

            handlers.put("deleteById", args -> {
                Long id = (Long) args[0];
                users.remove(id);
                return null;
            });
        }

        private void setupFunctionsRepo() {
            Map<String, Function<Object[], Object>> handlers = functionsRepoHandler.methodHandlers;
            Map<Long, Functions> functions = new HashMap<>();
            functions.put(1L, testFunction);

            handlers.put("findById", args -> {
                Long id = (Long) args[0];
                return Optional.ofNullable(functions.get(id));
            });

            handlers.put("findByNameAndUser", args -> {
                String name = (String) args[0];
                Users user = (Users) args[1];
                return functions.values().stream()
                        .filter(f -> f.getName().equals(name) && f.getUser().equals(user))
                        .findFirst();
            });

            handlers.put("existsByNameAndUser", args -> {
                String name = (String) args[0];
                Users user = (Users) args[1];
                return functions.values().stream()
                        .anyMatch(f -> f.getName().equals(name) && f.getUser().equals(user));
            });

            handlers.put("save", args -> {
                Functions func = (Functions) args[0];
                functions.put(func.getId(), func);
                return func;
            });

            handlers.put("deleteById", args -> {
                Long id = (Long) args[0];
                functions.remove(id);
                return null;
            });
        }

        private void setupPointsRepo() {
            Map<String, Function<Object[], Object>> handlers = pointsRepoHandler.methodHandlers;
            Map<Long, FunctionPoints> points = new HashMap<>();
            points.put(1L, testPoint);

            handlers.put("findById", args -> {
                Long id = (Long) args[0];
                return Optional.ofNullable(points.get(id));
            });

            handlers.put("findByIdAndFunction_Id", args -> {
                Long pointId = (Long) args[0];
                Long functionId = (Long) args[1];
                return points.values().stream()
                        .filter(p -> p.getId().equals(pointId) &&
                                p.getFunction().getId().equals(functionId))
                        .findFirst();
            });

            handlers.put("findByFunctionIdAndX", args -> {
                Long functionId = (Long) args[0];
                Double xValue = (Double) args[1];
                return points.values().stream()
                        .filter(p -> p.getFunction().getId().equals(functionId) &&
                                p.getX_value().equals(xValue))
                        .findFirst();
            });

            handlers.put("save", args -> {
                FunctionPoints point = (FunctionPoints) args[0];
                points.put(point.getId(), point);
                return point;
            });

            handlers.put("delete", args -> {
                FunctionPoints point = (FunctionPoints) args[0];
                points.remove(point.getId());
                return null;
            });
        }

        private void setupCompositeRepo() {
            Map<String, Function<Object[], Object>> handlers = compositeRepoHandler.methodHandlers;
            Map<Long, CompositeFunctionElements> elements = new HashMap<>();

            handlers.put("findByFunction", args -> {
                Functions function = (Functions) args[0];
                return elements.values().stream()
                        .filter(e -> e.getFunction().equals(function))
                        .toList();
            });

            handlers.put("save", args -> {
                CompositeFunctionElements element = (CompositeFunctionElements) args[0];
                elements.put(element.getId(), element);
                return element;
            });
        }
    }

    static class TestInvocationHandler implements java.lang.reflect.InvocationHandler {
        Map<String, Function<Object[], Object>> methodHandlers = new HashMap<>();

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
            String methodName = method.getName();

            if (method.getReturnType().equals(Optional.class)) {
                Function<Object[], Object> handler = methodHandlers.get(methodName);
                if (handler != null) {
                    Object result = handler.apply(args);
                    if (result instanceof Optional) {
                        return result;
                    }
                    return Optional.ofNullable(result);
                }
                return Optional.empty();
            }

            Function<Object[], Object> handler = methodHandlers.get(methodName);
            if (handler != null) {
                return handler.apply(args);
            }

            if (method.getReturnType().equals(void.class)) {
                return null;
            } else if (method.getReturnType().equals(boolean.class)) {
                return false;
            } else if (method.getReturnType().equals(long.class)) {
                return 0L;
            } else if (method.getReturnType().equals(int.class)) {
                return 0;
            } else if (method.getReturnType().equals(List.class)) {
                return Collections.emptyList();
            }

            return null;
        }
    }
}