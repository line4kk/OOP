package engine.service;

import engine.annotations.AnalyticalFunction;
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

    static {
        scanPackage("functions");
    }

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

    public static MathFunction getFunction(String name) {
        Objects.requireNonNull(name, "Имя функции не может быть null");
        MathFunction function = REGISTERED_FUNCTIONS.get(name);
        if (function == null) {
            throw new IllegalArgumentException("Аналитическая функция не найдена: " + name);
        }
        return function;
    }

    public static Map<String, MathFunction> getRegisteredFunctions() {
        return Collections.unmodifiableMap(REGISTERED_FUNCTIONS);
    }

    public static boolean isAnalyticalFunction(String name) {
        return REGISTERED_FUNCTIONS.containsKey(name);
    }
}
