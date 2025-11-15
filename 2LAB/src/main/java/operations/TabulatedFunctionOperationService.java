package operations;

import exceptions.InconsistentFunctionsException;
import functions.TabulatedFunction;
import functions.Point;
import functions.factory.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabulatedFunctionOperationService {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionOperationService.class);
    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            logger.error("Попытка преобразования null функции в точки");
            throw new IllegalArgumentException("Функция не была получена");
        }

        int count = tabulatedFunction.getCount();
        Point[] all_points = new Point[count];

        int i = 0;
        for (Point point : tabulatedFunction) {
            all_points[i] = point;
            i++;
        }

        logger.debug("Функция преобразована в {} точек", count);
        return all_points;
    }

    private TabulatedFunctionFactory factory;

    // Два конструктора для фабрики, геттер и сеттер
    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        if (factory == null) {
            logger.error("Попытка создать сервис с null фабрикой");
            throw new IllegalArgumentException("Фабрика не может быть пустой");
        }
        this.factory = factory;
        logger.debug("Создан сервис операций с фабрикой: {}", factory.getClass().getSimpleName());
    }

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
        logger.debug("Создан сервис операций с фабрикой по умолчанию");
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        if (factory == null) {
            logger.error("Попытка установить null фабрику");
            throw new IllegalArgumentException("Фабрика не может быть пустой");
        }
        logger.debug("Смена фабрики: {} -> {}", this.factory.getClass().getSimpleName(), factory.getClass().getSimpleName());
        this.factory = factory;
    }

    // Вложенный интерфейс операций и реализация самих операций
    private interface BiOperation {
        double apply(double u, double v);
    }

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation) {
        logger.debug("Выполнение операции над функциями с {} и {} точками", a.getCount(), b.getCount());
        // Проверка количества точек
        if (a.getCount() != b.getCount()) {
            logger.error("Несовпадение количества точек: {} != {}", a.getCount(), b.getCount());
            throw new InconsistentFunctionsException("Функции имеют разное количество точек");
        }

        // Получаем точки функций и создаём массивы
        Point[] points_A = asPoints(a);
        Point[] points_B = asPoints(b);

        double[] xValues = new double[a.getCount()];
        double[] yValues = new double[a.getCount()];

        // Выполняем операцию
        for (int i = 0; i < a.getCount(); i++) {
            if (points_A[i].x != points_B[i].x) {
                logger.error("Несовпадение x в точке {}: {} != {}", i, points_A[i].x, points_B[i].x);
                throw new InconsistentFunctionsException("Иксы не совпадают");
            }

            xValues[i] = points_A[i].x;
            yValues[i] = operation.apply(points_A[i].y, points_B[i].y);
            logger.trace("Точка {}: x={}, y={}", i, xValues[i], yValues[i]);
        }

        TabulatedFunction result = factory.create(xValues, yValues);
        logger.debug("Операция завершена, создана новая функция с {} точками", result.getCount());
        return factory.create(xValues, yValues);
    }
    // Сложение
    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        logger.debug("Сложение функций");
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u + v;
            }
        });
    }

    // Вычитание
    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        logger.debug("Вычитание функций");
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u - v;
            }
        });
    }

    // Умножение
    public TabulatedFunction multiplication(TabulatedFunction a, TabulatedFunction b) {
        logger.debug("Умножение функций");
        return doOperation(a, b, (u, v) -> u * v);
    }

    // Деление
    public TabulatedFunction division(TabulatedFunction a, TabulatedFunction b) {
        logger.debug("Деление функций");
        return doOperation(a, b, (u, v) -> u / v);
    }


}
