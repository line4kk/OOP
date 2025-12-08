package engine.service;

import engine.entity.*;
import engine.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class MultipleSearchServiceTest {

    private MultipleSearchService service;
    private TestData testData;

    @BeforeEach
    void setUp() {
        testData = new TestData();

        service = new MultipleSearchService();

        UsersRepository usersRepoProxy = createProxy(UsersRepository.class, testData.usersRepoHandler);
        FunctionsRepository functionsRepoProxy = createProxy(FunctionsRepository.class, testData.functionsRepoHandler);
        FunctionPointsRepository pointsRepoProxy = createProxy(FunctionPointsRepository.class, testData.pointsRepoHandler);
        OperationsRepository operationsRepoProxy = createProxy(OperationsRepository.class, testData.operationsRepoHandler);
        CompositeFunctionElementsRepository compositeRepoProxy = createProxy(CompositeFunctionElementsRepository.class, testData.compositeRepoHandler);

        setField(service, "usersRepository", usersRepoProxy);
        setField(service, "functionsRepository", functionsRepoProxy);
        setField(service, "functionPointsRepository", pointsRepoProxy);
        setField(service, "operationsRepository", operationsRepoProxy);
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
    void testFindAllUsers() {
        List<Users> result = service.findAllUsers();
        assertEquals(2, result.size());
    }

    @Test
    void testFindFunctionsByUser() {
        List<Functions> result = service.findFunctionsByUser("user1");
        assertEquals(2, result.size());
    }

    @Test
    void testFindFunctionsByUserNotFound() {
        List<Functions> result = service.findFunctionsByUser("nonExistent");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindFunctionsByName() {
        List<Functions> result = service.findFunctionsByName("func1");
        assertEquals(2, result.size());
    }

    @Test
    void testFindAllFunctions() {
        List<Functions> result = service.findAllFunctions();
        assertEquals(3, result.size());
    }

    @Test
    void testFindFunctionsByType() {
        List<Functions> result = service.findFunctionsByType("type1");
        assertEquals(2, result.size());
    }

    @Test
    void testFindPointsByFunction() {
        List<FunctionPoints> result = service.findPointsByFunction(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testFindCompositeFunctions() {
        List<Functions> result = service.findCompositeFunctions();
        assertEquals(1, result.size());
        assertEquals("composite", result.get(0).getSource());
    }

    @Test
    void testFindCompositeFunctionElementsByCompositeId() {
        List<CompositeFunctionElements> result = service.findCompositeFunctionElementsByCompositeId(1L);
        assertEquals(2, result.size());
    }

    @Test
    void testFindCompositeFunctionElementsByCompositeIdNotFound() {
        List<CompositeFunctionElements> result = service.findCompositeFunctionElementsByCompositeId(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindCompositeFunctionElementsOrdered() {
        List<CompositeFunctionElements> result = service.findCompositeFunctionElementsOrdered(1L);
        assertEquals(2, result.size());
        // Проверяем сортировку
        assertEquals(1, result.get(0).getFunctionOrder());
        assertEquals(2, result.get(1).getFunctionOrder());
    }

    static class TestData {
        Users user1;
        Users user2;
        Functions function1;
        Functions function2;
        Functions function3;

        TestInvocationHandler usersRepoHandler = new TestInvocationHandler();
        TestInvocationHandler functionsRepoHandler = new TestInvocationHandler();
        TestInvocationHandler pointsRepoHandler = new TestInvocationHandler();
        TestInvocationHandler operationsRepoHandler = new TestInvocationHandler();
        TestInvocationHandler compositeRepoHandler = new TestInvocationHandler();

        TestData() {
            user1 = new Users(1L, "user1", "hash1", "user", "array", new ArrayList<>());
            user2 = new Users(2L, "user2", "hash2", "admin", "linked", new ArrayList<>());

            function1 = new Functions();
            function1.setId(1L);
            function1.setUser(user1);
            function1.setName("func1");
            function1.setType("type1");
            function1.setSource("composite");

            function2 = new Functions();
            function2.setId(2L);
            function2.setUser(user1);
            function2.setName("func2");
            function2.setType("type2");
            function2.setSource("user");

            function3 = new Functions();
            function3.setId(3L);
            function3.setUser(user2);
            function3.setName("func1");
            function3.setType("type1");
            function3.setSource("user");

            setupUsersRepo();
            setupFunctionsRepo();
            setupPointsRepo();
            setupOperationsRepo();
            setupCompositeRepo();
        }

        private void setupUsersRepo() {
            Map<String, Function<Object[], Object>> handlers = usersRepoHandler.methodHandlers;
            List<Users> users = new ArrayList<>(Arrays.asList(user1, user2));
            handlers.put("findAll", args -> users);

            handlers.put("findByUsername", args -> {
                String username = (String) args[0];
                return users.stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst()
                        .orElse(null);
            });
        }

        private void setupFunctionsRepo() {
            Map<String, Function<Object[], Object>> handlers = functionsRepoHandler.methodHandlers;
            List<Functions> functions = new ArrayList<>(Arrays.asList(function1, function2, function3));
            handlers.put("findAll", args -> functions);

            handlers.put("findByUser", args -> {
                Users user = (Users) args[0];
                return functions.stream()
                        .filter(f -> f.getUser().equals(user))
                        .toList();
            });

            handlers.put("findByName", args -> {
                String name = (String) args[0];
                return functions.stream()
                        .filter(f -> f.getName().equals(name))
                        .toList();
            });

            handlers.put("findByType", args -> {
                String type = (String) args[0];
                return functions.stream()
                        .filter(f -> f.getType().equals(type))
                        .toList();
            });

            handlers.put("findById", args -> {
                Long id = (Long) args[0];
                return Optional.ofNullable(
                        functions.stream()
                                .filter(f -> f.getId().equals(id))
                                .findFirst()
                                .orElse(null)
                );
            });
        }

        private void setupPointsRepo() {
            Map<String, Function<Object[], Object>> handlers = pointsRepoHandler.methodHandlers;

            FunctionPoints point = new FunctionPoints();
            point.setId(1L);
            point.setFunction(function1);

            List<FunctionPoints> points = List.of(point);

            handlers.put("findByFunction_Id", args -> {
                Long functionId = (Long) args[0];
                return points.stream()
                        .filter(p -> p.getFunction().getId().equals(functionId))
                        .toList();
            });
        }

        private void setupOperationsRepo() {
        }

        private void setupCompositeRepo() {
            Map<String, Function<Object[], Object>> handlers = compositeRepoHandler.methodHandlers;

            CompositeFunctionElements element1 = new CompositeFunctionElements();
            element1.setId(1L);
            element1.setComposite(function1);
            element1.setFunction(function2);
            element1.setFunctionOrder(2);

            CompositeFunctionElements element2 = new CompositeFunctionElements();
            element2.setId(2L);
            element2.setComposite(function1);
            element2.setFunction(function3);
            element2.setFunctionOrder(1);

            List<CompositeFunctionElements> elements = new ArrayList<>(Arrays.asList(element1, element2));
            handlers.put("findByComposite", args -> {
                Functions composite = (Functions) args[0];
                return elements.stream()
                        .filter(e -> e.getComposite().equals(composite))
                        .collect(java.util.stream.Collectors.toList());
            });
        }
    }

    static class TestInvocationHandler implements java.lang.reflect.InvocationHandler {
        Map<String, Function<Object[], Object>> methodHandlers = new HashMap<>();

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
            String methodName = method.getName();

            Function<Object[], Object> handler = methodHandlers.get(methodName);
            if (handler != null) {
                Object result = handler.apply(args);
                if (method.getReturnType().equals(Optional.class) && !(result instanceof Optional)) {
                    return Optional.ofNullable(result);
                }
                return result;
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
            } else if (method.getReturnType().equals(Optional.class)) {
                return Optional.empty();
            }

            return null;
        }
    }
}