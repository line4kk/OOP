package functions;

import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrictTabulatedFunction implements TabulatedFunction {
    private static final Logger logger = LoggerFactory.getLogger(StrictTabulatedFunction.class);
    private final TabulatedFunction function;

    public StrictTabulatedFunction(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public double apply(double x) {
        int index = function.indexOfX(x);
        if (index == -1) {
            logger.error("Попытка интерполяции для x = {} в строгой функции", x);
            throw new UnsupportedOperationException("Нельзя интерполировать");
        }
        return function.getY(index);
    }

    // Delegated methods

    @Override
    public int getCount() {
        return function.getCount();
    }

    @Override
    public double getX(int index) {
        return function.getX(index);
    }

    @Override
    public double getY(int index) {
        return function.getY(index);
    }

    @Override
    public void setY(int index, double value) {
        function.setY(index, value);
    }

    @Override
    public int indexOfX(double x) {
        return function.indexOfX(x);
    }

    @Override
    public int indexOfY(double y) {
        return function.indexOfY(y);
    }

    @Override
    public double leftBound() {
        return function.leftBound();
    }

    @Override
    public double rightBound() {
        return function.rightBound();
    }

    @Override
    public Iterator<Point> iterator() {
        return function.iterator();
    }
}