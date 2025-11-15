package operations;

import concurrent.SynchronizedTabulatedFunction;
import functions.Point;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Дифференцированный оператор для табулированной функции
public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedDifferentialOperator.class);
    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {  // По умолчанию фабрика
        factory = new ArrayTabulatedFunctionFactory();
        logger.debug("Создан табулированный дифференциальный оператор с фабрикой по умолчанию");
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {  // Фабрика передается в конструкторе
        this.factory = factory;
        logger.debug("Создан табулированный дифференциальный оператор с фабрикой: {}", factory.getClass().getSimpleName());

    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        logger.debug("Изменение фабрики: {} -> {}", this.factory.getClass().getSimpleName(), factory.getClass().getSimpleName());
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        logger.debug("Вычисление производной для табулированной функции с {} точками", function.getCount());
        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        double[] xValues = new double[points.length];  // Точки функции
        double[] yValues = new double[points.length];  // Значения производных функции от точек

        for (int i = 0; i < points.length - 1; i++) {  // Находим правую разностную производную для всех, точек, кроме последней
            xValues[i] = points[i].x;
            yValues[i] = (points[i+1].y - points[i].y) / (points[i+1].x - points[i].x);
            logger.trace("Производная в точке {}: {}", xValues[i], yValues[i]);
        }
        xValues[points.length - 1] = points[points.length - 1].x;
        yValues[points.length - 1] = yValues[points.length - 2];  // Производная последней точки - левая разностная = предпоследняя

        TabulatedFunction derivative = factory.create(xValues, yValues);
        logger.debug("Производная вычислена, создана новая функция с {} точками", derivative.getCount());
        return factory.create(xValues, yValues);

    }

    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {

        logger.debug("Синхронное вычисление производной для функции {}", function.getClass().getSimpleName());
        if (function instanceof SynchronizedTabulatedFunction) {
            return ((SynchronizedTabulatedFunction) function).doSynchronously(f -> derive(f));
        } else {
            return new SynchronizedTabulatedFunction(function).doSynchronously(f -> derive(f));
        }
    }

}
