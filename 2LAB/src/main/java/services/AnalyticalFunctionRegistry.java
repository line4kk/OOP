package services;

import annotations.AnalyticalFunction;
import functions.MathFunction;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AnalyticalFunctionRegistry {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticalFunctionRegistry.class);
    private static final Map<String, MathFunction> REGISTERED_FUNCTIONS = new HashMap<>();
    private static final String FUNCTIONS_PACKAGE = "functions";

    private AnalyticalFunctionRegistry() {
    }

    private static void scanPackage(String basePackage) {
        try {
            Reflections reflections = new Reflections(basePackage);
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(AnalyticalFunction.class);

            for (Class<?> candidate : annotatedClasses) {
                Class<? extends MathFunction> mathFunctionClass = candidate.asSubclass(MathFunction.class);
                MathFunction instance = mathFunctionClass.getDeclaredConstructor().newInstance();
                REGISTERED_FUNCTIONS.put(candidate.getSimpleName(), instance);
                logger.info("Зарегистрирована аналитическая функция: {}", candidate.getSimpleName());
            }
        } catch (Exception e) {
            logger.error("Ошибка при инициализации аналитических функций", e);
            throw new RuntimeException("Не удалось инициализировать аналитические функции", e);
        }
    }

    private static synchronized void ensureInitialized() {
        if (REGISTERED_FUNCTIONS.isEmpty()) {
            reload();
        }
    }

    public static synchronized void reload() {
        REGISTERED_FUNCTIONS.clear();
        scanPackage(FUNCTIONS_PACKAGE);
    }

    public static MathFunction getFunction(String name) {
        Objects.requireNonNull(name, "Имя функции не может быть null");
        ensureInitialized();

        MathFunction function = REGISTERED_FUNCTIONS.get(name);
        if (function == null) {
            logger.info("Функция {} не найдена, обновляем список аналитических функций", name);
            reload();
            function = REGISTERED_FUNCTIONS.get(name);
        }

        if (function == null) {
            throw new IllegalArgumentException("Аналитическая функция не найдена: " + name);
        }
        return function;
    }

    public static Map<String, MathFunction> getRegisteredFunctions() {
        ensureInitialized();
        return Collections.unmodifiableMap(REGISTERED_FUNCTIONS);
    }
}