package functions;
import exceptions.InterpolationException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

// Класс для хранения данных в массиве
public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable{

    @Serial
    private static final long serialVersionUID = -6372191640806238470L;

    private double[] xValues;
    private double[] yValues;
    private int count;

    // Конструктор класса с двумя параметрами типа double[]
    public ArrayTabulatedFunction(double[] xValues, double[] yValues){
        if (xValues.length < 2) {
            throw new IllegalArgumentException("В массиве меньше 2 элементов");
        }

        checkLengthIsTheSame(xValues, yValues);
        checkSorted(xValues);

        this.count = xValues.length;
        this.xValues = Arrays.copyOf(xValues, count);
        this.yValues = Arrays.copyOf(yValues, count);
    }

    // Конструктор класса с 4 параметрами
    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {

        if (count < 2) {
            throw new IllegalArgumentException("В массиве меньше 2 элементов");
        }
        // Если границы перепутаны
        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }
        this.count = count;
        this.xValues = new double[count];
        this.yValues = new double[count];

        if (xFrom == xTo) {
            Arrays.fill(xValues, xFrom);
            Arrays.fill(yValues, source.apply(xFrom));
        }
        else {
            // Равномерная дискретизация
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                xValues[i] = xFrom + i * step;
                yValues[i] = source.apply(xValues[i]);
            }
        }
    }
    // Далее перегрузки методов AbstractTabulatedFunction
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс не в диапазоне");
        }
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс не в диапазоне");
        }
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс не в диапазоне");
        }
        yValues[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(xValues[i] - x) < 1e-14) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(yValues[i] - y) < 1e-14) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public double leftBound() {
        return xValues[0];
    }

    @Override
    public double rightBound() {
        return xValues[count - 1];
    }

    @Override
    public int floorIndexOfX(double x) {
        // Когда все значения больше x
        if (x < xValues[0]) {
            throw new IllegalArgumentException();
        }

        // Когда x больше всех значений
        if (x > xValues[count - 1]) {
            return count;
        }

        // Поиск интервала для x
        for (int i = 0; i < count - 1; i++) {
            if (x >= xValues[i] && x < xValues[i + 1]) {
                return i;
            }
        }

        return count - 1;
    }

    @Override
    public double extrapolateLeft(double x) {
        if (count == 1) {
            return yValues[0];
        }
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    public double extrapolateRight(double x) {
        if (count == 1) {
            return yValues[0];
        }
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

    public double interpolate(double x, int floorIndex) {
        if (floorIndex < 0 || floorIndex >= count - 1) {
            throw new IndexOutOfBoundsException("Индекс не в диапазоне");
        }
        double x1 = xValues[floorIndex];
        double x2 = xValues[floorIndex + 1];
        double y1 = yValues[floorIndex];
        double y2 = yValues[floorIndex + 1];

        if (x < x1 || x > x2) {
            throw new InterpolationException("x не попадает в диапазон от " + x1 + " до " + x2);
        }

        // Интерполяция между floorIndex и floorIndex + 1
        return interpolate(x, x1, x2, y1, y2);
    }

    @Override
    public double apply(double x) {
        if (x < leftBound()) {
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            return extrapolateRight(x);
        } else {
            int index = indexOfX(x);
            if (index != -1) {
                return getY(index);
            } else {
                return interpolate(x, floorIndexOfX(x));
            }
        }
    }

    @Override
    public void insert(double x, double y){
        int xIndex = indexOfX(x);
        if (xIndex != -1)
            setY(xIndex, y);
        else{
            double[] newXValues = new double[count+1];
            double[] newYValues = new double[count+1];

            int insertIndex = 0;
            while (insertIndex < count && x > xValues[insertIndex]) {
                insertIndex++;
            }

            // Копируем элементы до позиции вставки
            if (insertIndex > 0) {
                System.arraycopy(xValues, 0, newXValues, 0, insertIndex);
                System.arraycopy(yValues, 0, newYValues, 0, insertIndex);
            }

            // Вставляем новый элемент
            newXValues[insertIndex] = x;
            newYValues[insertIndex] = y;

            // Копируем оставшиеся элементы
            if (insertIndex < count) {
                System.arraycopy(xValues, insertIndex, newXValues, insertIndex + 1, count - insertIndex);
                System.arraycopy(yValues, insertIndex, newYValues, insertIndex + 1, count - insertIndex);
            }

            this.xValues = newXValues;
            this.yValues = newYValues;
            this.count++;

        }

    }
    @Override
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс выходит за размер");
        }

        for (int i = index; i < count - 1; i++) {
            xValues[i] = xValues[i + 1];
            yValues[i] = yValues[i + 1];
        }

        count--;
    }

    @Override
    public Iterator<Point> iterator() {
        Iterator<Point> iter = new Iterator<Point>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                else {
                    Point point = new Point(xValues[i], yValues[i]);
                    i++;
                    return point;
                }
            }
        };
        return iter;
    }
}
