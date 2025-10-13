package operations;

import exceptions.InconsistentFunctionsException;
import functions.TabulatedFunction;
import functions.Point;
import functions.factory.*;

public class TabulatedFunctionOperationService {
    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new IllegalArgumentException("Функция не была получена");
        }

        int count = tabulatedFunction.getCount();
        Point[] all_points = new Point[count];

        int i = 0;
        for (Point point : tabulatedFunction) {
            all_points[i] = point;
            i++;
        }

        return all_points;
    }

    private TabulatedFunctionFactory factory;

    // Два конструктора для фабрики, геттер и сеттер
    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Фабрика не может быть пустой");
        }
        this.factory = factory;
    }

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Фабрика не может быть пустой");
        }
        this.factory = factory;
    }

    // Вложенный интерфейс операций и реализация самих операций
    private interface BiOperation {
        double apply(double u, double v);
    }

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation) {
        // Проверка количества точек
        if (a.getCount() != b.getCount()) {
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
                throw new InconsistentFunctionsException("Иксы не совпадают");
            }

            xValues[i] = points_A[i].x;
            yValues[i] = operation.apply(points_A[i].y, points_B[i].y);
        }

        return factory.create(xValues, yValues);
    }
    // Сложение
    public TabulatedFunction add(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u + v;
            }
        });
    }

    // Вычитание
    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, new BiOperation() {
            @Override
            public double apply(double u, double v) {
                return u - v;
            }
        });
    }

    // Умножение
    public TabulatedFunction multiplication(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u * v);
    }

    // Деление
    public TabulatedFunction devision(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u / v);
    }


}
