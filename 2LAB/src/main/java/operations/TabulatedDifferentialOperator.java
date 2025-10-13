package operations;

import functions.Point;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;

// Дифференцированный оператор для табулированной функции
public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {
    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {  // По умолчанию фабрика
        factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {  // Фабрика передается в конструкторе
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        double[] xValues = new double[points.length];  // Точки функции
        double[] yValues = new double[points.length];  // Значения производных функции от точек

        for (int i = 0; i < points.length - 1; i++) {  // Находим правую разностную производную для всех, точек, кроме последней
            xValues[i] = points[i].x;
            yValues[i] = (points[i+1].y - points[i].y) / (points[i+1].x - points[i].x);
        }
        xValues[points.length - 1] = points[points.length - 1].x;
        yValues[points.length - 1] = yValues[points.length - 2];  // Производная последней точки - левая разностная = предпоследняя

        return factory.create(xValues, yValues);

    }

}
