package concurrent;

import functions.Point;
import functions.TabulatedFunction;
import operations.TabulatedFunctionOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private final TabulatedFunction function;
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedTabulatedFunction.class);
    public SynchronizedTabulatedFunction(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public synchronized int getCount() {
        return function.getCount();
    }

    @Override
    public synchronized double getX(int index) {
        return function.getX(index);
    }

    @Override
    public synchronized double getY(int index) {
        return function.getY(index);
    }

    @Override
    public synchronized void setY(int index, double value) {
        function.setY(index, value);
    }

    @Override
    public synchronized int indexOfX(double x) {
        return function.indexOfX(x);
    }

    @Override
    public synchronized int indexOfY(double y) {
        return function.indexOfY(y);
    }

    @Override
    public synchronized double leftBound() {
        return function.leftBound();
    }

    @Override
    public synchronized double rightBound() {
        return function.rightBound();
    }

    @Override
    public synchronized double apply(double x) {
        return function.apply(x);
    }

    @Override
    public synchronized Iterator<Point> iterator() {
        logger.debug("Создание итератора для функции");
        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        return new Iterator<Point>() {
            int ind = 0;

            @Override
            public boolean hasNext() {
                return ind < getCount();
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    logger.error("Попытка получить следующий элемент за пределами итератора");
                    throw new NoSuchElementException();
                }

                else {
                    logger.trace("Итератор: получена точка {}", ind);
                    return points[ind++];
                }
            }
        };
    }

    public interface Operation<T> {
        T apply(SynchronizedTabulatedFunction function);
    }

    // Операция - производитель значений типа T
    public <T> T doSynchronously(Operation<? extends T> operation) {
        logger.debug("Выполнение синхронной операции в потоке {}", Thread.currentThread().getName());
        synchronized (function) {
            return operation.apply(this);
        }
    }
}
