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
import service.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = config.TestConfig.class, loader = AnnotationConfigContextLoader.class)
public class AllSortServiceTest {

    @Autowired private UsersRepository usersRepository;
    @Autowired private FunctionsRepository functionsRepository;
    @Autowired private FunctionPointsRepository functionPointsRepository;
    @Autowired private OperationsRepository operationsRepository;
    @Autowired private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    @Autowired private SingleSearchService singleSearchService;
    @Autowired private MultipleSearchService multipleSearchService;
    @Autowired private SortedSearchService sortedSearchService;

    private List<String[]> results = new ArrayList<>();
    private Users testUser;
    private Functions testFunction;
    private FunctionPoints testPoint;
    int test_unit = 4444;

    @BeforeEach
    public void generateTestData() {
        // Очистка данных
        operationsRepository.deleteAll();
        compositeFunctionElementsRepository.deleteAll();
        functionPointsRepository.deleteAll();
        functionsRepository.deleteAll();
        usersRepository.deleteAll();

        List<Users> users = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            users.add(new Users("user" + i, "hash" + i, i % 2 == 0 ? "admin" : "user",
                    i % 2 == 0 ? "linked_list" : "array"));
        }
        usersRepository.saveAll(users);
        testUser = users.get(test_unit);

        List<Functions> functions = new ArrayList<>();
        String[] types = {"linked_list_tabulated", "array_tabulated", "analytical"};
        String[] sources = {"base", "operations", "composite"};

        for (int i = 0; i < 10000; i++) {
            Users user = users.get(i % users.size());
            functions.add(new Functions(user, "function_" + i, types[i % types.length], sources[i % sources.length]));
        }
        functionsRepository.saveAll(functions);
        testFunction = functions.get(test_unit);

        List<FunctionPoints> points = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Functions function = functions.get(i % functions.size());
            points.add(new FunctionPoints(function, (double)i, (double)i * 2));
        }
        functionPointsRepository.saveAll(points);
        testPoint = points.get(test_unit);

        List<Operations> operations = new ArrayList<>();
        String[] operationTypes = {"addition", "subtraction", "multiplication", "division"};
        for (int i = 0; i < 10000; i++) {
            FunctionPoints point1 = points.get(i % points.size());
            FunctionPoints point2 = points.get((i + 1) % points.size());
            operations.add(new Operations(operationTypes[i % operationTypes.length], point1, point2, (double)i));
        }
        operationsRepository.saveAll(operations);

        List<CompositeFunctionElements> composites = new ArrayList<>();
        List<Functions> compositeFunctions = functions.stream()
                .filter(f -> "composite".equals(f.getSource()))
                .toList();
        List<Functions> elementFunctions = functions.stream()
                .filter(f -> "base".equals(f.getSource()) || "operations".equals(f.getSource()))
                .toList();

        for (int i = 0; i < 10000 && i < compositeFunctions.size() && i < elementFunctions.size(); i++) {
            Functions composite = compositeFunctions.get(i % compositeFunctions.size());
            Functions element = elementFunctions.get(i % elementFunctions.size());
            composites.add(new CompositeFunctionElements(composite, i % 5, element));
        }
        compositeFunctionElementsRepository.saveAll(composites);

        results.add(new String[]{"Sort_productivity", "Time(ms)"});
    }


    @Test
    public void testSearches() {
        // SingleSearchService

        // Одиночный поиск пользователей
        time_measurement("single_user_by_username", () -> {
            singleSearchService.findUserByUsername("user100");
        });

        time_measurement("single_user_by_id", () -> {
            singleSearchService.findUserById(1L);
        });

        // Одиночный поиск функций
        time_measurement("single_function_by_id", () -> {
            singleSearchService.findFunctionById(testFunction.getId());
        });

        // Одиночный поиск операций
        time_measurement("single_operation_by_id", () -> {
            singleSearchService.findOperationById(1L);
        });

        // Одиночный поиск композитных элементов
        time_measurement("single_composite_element_by_id", () -> {
            singleSearchService.findCompositeElementById(1L);
        });

        // MultipleSearchService

        // Множественный поиск всех сущностей
        time_measurement("multiple_all_users", () -> {
            multipleSearchService.findAllUsers();
        });

        // Множественный поиск функций
        time_measurement("multiple_functions_by_user", () -> {
            multipleSearchService.findFunctionsByUser("user50");
        });

        time_measurement("multiple_functions_by_type", () -> {
            multipleSearchService.findFunctionsByType("linked_list_tabulated");
        });

        time_measurement("multiple_points_by_function", () -> {
            multipleSearchService.findPointsByFunction(testFunction.getId());
        });

        // Множественный поиск композитных данных
        time_measurement("multiple_composite_functions", () -> {
            multipleSearchService.findCompositeFunctions();
        });

        // SortedSearchService

        // Сортировка пользователей
        time_measurement("sorted_users_by_username", () -> {
            sortedSearchService.findUsersSortedByUsername();
        });

        // Сортировка функций
        time_measurement("sorted_functions_by_name", () -> {
            sortedSearchService.findFunctionsSortedByName();
        });

        time_measurement("sorted_functions_by_type", () -> {
            sortedSearchService.findFunctionsSortedByType();
        });

        // Сортировка точек
        time_measurement("sorted_points_by_x", () -> {
            sortedSearchService.findPointsSortedByX(testFunction.getId());
        });

        time_measurement("sorted_points_by_y", () -> {
            sortedSearchService.findPointsSortedByY(testFunction.getId());
        });

        saveResults();
    }

    private void time_measurement(String searchType, Runnable searchOperation) {
        try {
            long startTime = System.nanoTime();
            searchOperation.run();
            long endTime = System.nanoTime();
            double timeMs = (endTime - startTime) / 1000000.0;
            results.add(new String[]{searchType, String.format("%.2f", timeMs)});
        } catch (Exception e) {
            results.add(new String[]{searchType, "ERROR"});
        }
    }

    private void saveResults() {
        try (FileWriter writer = new FileWriter("sort_service_result.txt")) {
            writer.write('\uFEFF');
            for (String[] record : results) {
                writer.write(String.join(",", record));
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing txt: " + e.getMessage());
        }
    }

}
