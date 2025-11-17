package productivity;

import entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import repository.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = config.TestConfig.class, loader = AnnotationConfigContextLoader.class)
public class ProductivityTest {

    @Autowired private UsersRepository usersRepository;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;
    @Autowired private OperationsRepository operationsRepository;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    private List<String[]> results = new ArrayList<>();
    private Users testUser;
    private Functions testFunction, testCompositeFunction;
    private FunctionPoints testPoint;

    @BeforeEach
    public void generateMassiveTestData() {
        // Если до этого были данные то они очищаются
        operationsRepository.deleteAll();
        compositeFunctionElementsRepository.deleteAll();
        functionPointsRepository.deleteAll();
        functionsRepository.deleteAll();
        usersRepository.deleteAll();
        int test_unit = 4444;

        // Генерация 10000 пользователей
        List<Users> users = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            users.add(new Users("user" + i, "hash" + i, i % 2 == 0 ? "admin" : "user", i % 2 == 0 ? "linked_list" : "array"));
        }
        usersRepository.saveAll(users);
        testUser = users.get(test_unit);

        // Генерация 10000 функций
        List<Functions> functions = new ArrayList<>();
        String[] types = {"linked_list_tabulated", "array_tabulated", "analytical"};
        String[] sources = {"base", "operations", "composite"};

        for (int i = 0; i < 10000; i++) {
            Users user = users.get(i % users.size());
            functions.add(new Functions(user, "function_" + i, types[i % types.length], sources[i % sources.length]));
        }
        functionsRepository.saveAll(functions);
        testFunction = functions.stream().filter(f -> "base".equals(f.getSource())).findFirst().orElse(functions.get(test_unit - 1));
        testCompositeFunction = functions.stream().filter(f -> "composite".equals(f.getSource())).findFirst().orElse(functions.get(test_unit + 1));

        // Генерация 10000 точек функций
        List<FunctionPoints> points = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Functions function = functions.get(i % functions.size());
            points.add(new FunctionPoints(function, (double)i, (double)i * 2));
        }
        functionPointsRepository.saveAll(points);
        testPoint = points.get(test_unit);

        // Генерация 10000 операций
        List<Operations> operations = new ArrayList<>();
        String[] operationTypes = {"addition", "subtraction", "multiplication", "division"};
        for (int i = 0; i < 10000; i++) {
            FunctionPoints point1 = points.get(i % points.size());
            FunctionPoints point2 = points.get((i + 1) % points.size());
            operations.add(new Operations(operationTypes[i % operationTypes.length], point1, point2, (double)i));
        }
        operationsRepository.saveAll(operations);

        // Генерация 10000 композитных функций
        List<CompositeFunctionElements> composites = new ArrayList<>();
        List<Functions> compositeFunctions = new ArrayList<>();
        List<Functions> elementFunctions = new ArrayList<>();

        for (Functions function : functions) {
            String source = function.getSource();
            if ("composite".equals(source)) {
                compositeFunctions.add(function);
            }
            if ("base".equals(source) || "operations".equals(source)) {
                elementFunctions.add(function);
            }
        }

        for (int i = 0; i < 10000; i++) {
            Functions composite = compositeFunctions.get(i % compositeFunctions.size());
            Functions element = elementFunctions.get(i % elementFunctions.size());
            composites.add(new CompositeFunctionElements(composite, i % 5, element));
        }
        compositeFunctionElementsRepository.saveAll(composites);

        results.add(new String[]{"SQL_Operation", "Time(ms)"});
    }

    @Test
    public void run10kRecords() {
        // Тест для 10000 пользователей
        time_measurement("user_insert",
                () ->  usersRepository.save(new Users("test_user", "test_hash", "user", "linked_list")));
        time_measurement("user_findByUsername",
                () -> usersRepository.findByUsername("user5000"));
        time_measurement("user_updateRole",
                () -> {Users user = usersRepository.findByUsername("user1000");
            user.setRole("admin");
            usersRepository.save(user);
        });
        time_measurement("user_findByUsername_with_password",
                () -> {Users user = usersRepository.findByUsername("user5000");
            String passwordHash = user.getPasswordHash();
        });

        time_measurement("user_updateUsername",
                () -> {Users user = usersRepository.findByUsername("user2000");
            user.setUsername("updated_username");
            usersRepository.save(user);
        });

        time_measurement("user_updatePasswordHash",
                () -> {Users user = usersRepository.findByUsername("user3000");
            user.setPasswordHash("new_hash");
            usersRepository.save(user);
        });

        time_measurement("user_updateFactoryType",
                () -> {Users user = usersRepository.findByUsername("user4000");
            user.setFactoryType("array");
            usersRepository.save(user);
        });
        time_measurement("user_delete",
                () -> {Users userToDelete = usersRepository.findByUsername("user9999");
            usersRepository.delete(userToDelete);
        });

        // Тест для 10000 функций
        time_measurement("function_insert",
                () -> functionsRepository.save(new Functions(testUser, "new_func", "analytical", "base")));
        time_measurement("function_findByUserId",
                () -> functionsRepository.findByUser(testUser));
        time_measurement("function_findByType",
                () -> functionsRepository.findByType("linked_list_tabulated"));
        time_measurement("function_updateName",
                () -> {testFunction.setName("updated_name");
            functionsRepository.save(testFunction);
        });
        time_measurement("function_delete_by_userId",
                () -> {List<Functions> userFunctions = functionsRepository.findByUser(testUser);
            functionsRepository.deleteAll(userFunctions);
        });

        // Тест для 10000 точек функций
        time_measurement("points_insert",
                () -> functionPointsRepository.save(new FunctionPoints(testFunction, 9999.0, 9999.0)));
        time_measurement("points_findByFunction",
                () -> functionPointsRepository.findByFunction(testFunction));
        time_measurement("points_updateYValue",
                () -> {testPoint.setYValue(8888.0);
            functionPointsRepository.save(testPoint);
        });
        time_measurement("points_delete_by_functionId",
                () -> {List<FunctionPoints> points = functionPointsRepository.findByFunction(testFunction);
            functionPointsRepository.deleteAll(points);
        });

        // Тест для 10000 операций
        time_measurement("operations_insert",
                () -> operationsRepository.save(new Operations("addition", testPoint, testPoint, 100.0)));
        time_measurement("operations_findByPoint1",
                () -> operationsRepository.findByPoint1(testPoint));
        time_measurement("operations_complex_find",
                () -> {List<Operations> byPoint1 = operationsRepository.findByPoint1(testPoint);
            List<Operations> byPoint2 = operationsRepository.findByPoint2(testPoint);
            List<Operations> result = new ArrayList<>();
            result.addAll(byPoint1);
            result.addAll(byPoint2);
        });

        time_measurement("operations_delete_complex",
                () -> {List<Operations> byPoint1 = operationsRepository.findByPoint1(testPoint);
            List<Operations> byPoint2 = operationsRepository.findByPoint2(testPoint);
            if (!byPoint1.isEmpty()) operationsRepository.delete(byPoint1.get(0));
            if (!byPoint2.isEmpty()) operationsRepository.delete(byPoint2.get(0));
        });
        time_measurement("operations_delete",
                () -> {List<Operations> operations = operationsRepository.findAll();
            if (!operations.isEmpty()) operationsRepository.delete(operations.get(0));
        });

        // Тест для 10000 композитных функций
        time_measurement("composite_insert",
                () -> compositeFunctionElementsRepository.save(new CompositeFunctionElements(testCompositeFunction, 10, testFunction)));
        time_measurement("composite_findByComposite",
                () -> compositeFunctionElementsRepository.findByComposite(testCompositeFunction));
        time_measurement("composite_delete",
                () -> {List<CompositeFunctionElements> composites = compositeFunctionElementsRepository.findAll();
            if (!composites.isEmpty()) compositeFunctionElementsRepository.delete(composites.get(0));
        });

        saveResults();
    }

    private void time_measurement(String operationName, Runnable sqlOperation) {
        try {
            long startTime = System.nanoTime();
            sqlOperation.run();
            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1000000.0;
            results.add(new String[]{operationName, String.format("%.2f", timeMs)});
        } catch (Exception e) {
            results.add(new String[]{operationName, "ERROR"});
        }
    }

    private void saveResults() {
        try (FileWriter writer = new FileWriter("productivity_results.csv")) {
            writer.write('\uFEFF');
            for (String[] record : results) {
                writer.write(String.join(",", record));
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing CSV: " + e.getMessage());
        }
    }
}
