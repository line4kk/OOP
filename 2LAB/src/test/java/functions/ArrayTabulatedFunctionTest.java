package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayTabulatedFunctionTest {
    // Тесты конструкторов
    @Test
    public void testConstructorWithArrays() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(5, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(5.0, function.rightBound(), 1e-10);
    }

    @Test
    public void testConstructorWithMathFunction() {
        MathFunction source = x -> x * x;
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(source, 0.0, 4.0, 5);

        assertEquals(5, func.getCount());
        assertEquals(0.0, func.getX(0), 1e-10);
        assertEquals(4.0, func.getX(4), 1e-10);
        assertEquals(0.0, func.getY(0), 1e-10);
        assertEquals(16.0, func.getY(4), 1e-10);
    }

    @Test
    public void testConstructorWithSwappedBounds() {
        MathFunction source = x -> x + 1;
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(source, 5.0, 1.0, 5);

        assertEquals(1.0, func.getX(0), 1e-10);
        assertEquals(5.0, func.getX(4), 1e-10);
    }

    @Test
    public void testConstructorWithEqualBounds() {
        MathFunction source = x -> x * 2;
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(source, 3.0, 3.0, 4);

        assertEquals(3.0, func.getX(0), 1e-10);
        assertEquals(3.0, func.getX(3), 1e-10);
        assertEquals(6.0, func.getY(0), 1e-10);
        assertEquals(6.0, func.getY(3), 1e-10);
    }

    // Тесты методов доступа
    @Test
    public void testGetCount() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(3, function.getCount());
    }

    @Test
    public void testGetX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
    }

    @Test
    public void testGetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4.0, function.getY(0), 1e-10);
        assertEquals(5.0, function.getY(1), 1e-10);
        assertEquals(6.0, function.getY(2), 1e-10);
    }

    @Test
    public void testSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.setY(1, 15.0);
        assertEquals(15.0, function.getY(1), 1e-10);
    }

    // Тесты поиска
    @Test
    public void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(2, function.indexOfX(3.0));
        assertEquals(-1, function.indexOfX(1.5));
    }

    @Test
    public void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfY(2.0));
        assertEquals(2, function.indexOfY(6.0));
        assertEquals(-1, function.indexOfY(5.0));
    }

    @Test
    public void testFloorIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0, 10.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(0.5));   // Меньше всех
        assertEquals(0, function.floorIndexOfX(1.0));   // Равно первому
        assertEquals(0, function.floorIndexOfX(1.5));   // Между 1 и 2
        assertEquals(1, function.floorIndexOfX(2.5));   // Между 2 и 3
        assertEquals(3, function.floorIndexOfX(4.5));   // Между 4 и 5
        assertEquals(4, function.floorIndexOfX(5.0));   // Равно последнему
        assertEquals(5, function.floorIndexOfX(6.0));   // Больше всех
    }

    // Тесты границ
    @Test
    public void testLeftBound() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.leftBound(), 1e-10);
    }

    @Test
    public void testRightBound() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {4.0, 5.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    // Тесты интерполяции и экстраполяции
    @Test
    public void testExtrapolateLeft() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.extrapolateLeft(0.0);
        double expected = 0.0; // Экстраполяция слева: y = 2x
        assertEquals(expected, result, 1e-10);
    }

    @Test
    public void testExtrapolateRight() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.extrapolateRight(4.0);
        double expected = 8.0; // Экстраполяция справа: y = 2x
        assertEquals(expected, result, 1e-10);
    }

    @Test
    public void testInterpolateWithIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.interpolate(1.5, 0); // Между 1.0 и 2.0
        assertEquals(3.0, result, 1e-10);
    }

    @Test
    public void testApplyWithExactX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(8.0, function.apply(4.0), 1e-10);
    }

    @Test
    public void testApplyWithInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(3.0, function.apply(1.5), 1e-10); // Между 1 и 2
        assertEquals(5.0, function.apply(2.5), 1e-10); // Между 2 и 3
        assertEquals(7.0, function.apply(3.5), 1e-10); // Между 3 и 4
    }

    @Test
    public void testApplyWithExtrapolation() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.0, function.apply(0.0), 1e-10);  // Слева
        assertEquals(8.0, function.apply(4.0), 1e-10); // Справа
    }

    @Test
    public void testInterpolateMethod() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {2.0, 4.0, 6.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        double result = function.interpolate(1.5, 1.0, 2.0, 2.0, 4.0);
        assertEquals(3.0, result, 1e-10);
    }

    @Test
    public void testLinearFunction() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 2.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0.5, function.apply(0.5), 1e-10);
        assertEquals(1.5, function.apply(1.5), 1e-10);
        assertEquals(-0.5, function.apply(-0.5), 1e-10);
        assertEquals(2.5, function.apply(2.5), 1e-10);
    }

    @Test
    public void testSquareFunction() {
        MathFunction source = x -> x * x;
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(source, 0.0, 2.0, 3);

        assertEquals(0.0, function.getY(0), 1e-10);
        assertEquals(1.0, function.getY(1), 1e-10);
        assertEquals(4.0, function.getY(2), 1e-10);
    }
}