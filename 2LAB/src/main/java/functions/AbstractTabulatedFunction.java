package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import operations.TabulatedFunctionOperationService;

// Абстрактный класс для табличных функций, реализующий общую логику интерполяции и экстраполяции
public abstract  class AbstractTabulatedFunction implements TabulatedFunction{

    protected int count = 0; // Приватное поле count

    protected abstract int floorIndexOfX(double x); // поиск интеравала x
    protected abstract double extrapolateLeft(double x); // экстраполяция слева
    protected abstract double extrapolateRight(double x); // экстраполяция справа
    protected abstract double interpolate(double x, int floorIndex); // интерполяция с указанием индекса интервала

    // Защищённый метод с реализацией интерполяции
    protected double interpolate(double x, double leftX, double rightX,
                                 double leftY, double rightY) {
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    // Проверка на исключение когда длины массивов x и y разные
    static void checkLengthIsTheSame(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new DifferentLengthOfArraysException("Разная длина массивов");
        }
    }
    // Проверка на исключение когда в массиве x значения расположены не по возрастанию
    static void checkSorted(double[] xValues) {
        for (int i = 0; i < xValues.length - 1; i++) {
            if (xValues[i] >= xValues[i + 1]) {
                throw new ArrayIsNotSortedException("Массив не отсортирован");
            }
        }
    }

    //Абстрактные методы из TabulatedFunction
    public abstract int getCount();  // Метод получения количества табулированных значений
    public abstract double getX(int index);  // Метод, получающий значение аргумента x по номеру индекса
    public abstract double getY(int index);  // Метод, получающий значение y по номеру индекса:
    public abstract void setY(int index, double value);  // Метод, задающий значение y по номеру индекса
    public abstract int indexOfX(double x);  // Метод, возвращающий индекс аргумента x. Предполагается, что все x различны. Если такого x в таблице нет, то необходимо вернуть -1
    public abstract int indexOfY(double y);  // Метод, возвращающий индекс первого вхождения значения y. Если такого y в таблице нет, то необходимо вернуть -1:
    public abstract double leftBound();  // Метод, возвращающий самый левый x
    public abstract double rightBound();  // Метод, возвращающий самый правый x

    @Override
    public double apply(double x) {
        // x меньше левой границы
        if (x < getX(0)) {
            return extrapolateLeft(x);
        }

        // x больше правой границы
        if (x > getX(count - 1)) {
            return extrapolateRight(x);
        }

        // x внутри интервала
        int exactIndex = indexOfX(x);
        if (exactIndex != -1) {
            return getY(exactIndex);
        }

        // Вызов метода интерполяции
        int floorIndex = floorIndexOfX(x);
        return interpolate(x, floorIndex);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(getClass().getSimpleName());
        builder.append(" size = ").append(this.getCount());

        Point[] points = TabulatedFunctionOperationService.asPoints(this);

        for (Point point : points) {
            builder.append("\n");
            builder.append("[").append(point.x).append("; ").append(point.y).append("]");
        }

        return builder.toString();
    }
}
