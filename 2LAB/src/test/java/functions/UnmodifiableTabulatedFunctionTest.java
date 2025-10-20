package functions;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class UnmodifiableTabulatedFunctionTest {

    @Test
    // Проверка всех переопределённых функций array в декораторе, который запрещает модификацию значений
    public void testUnmodifiableTabulatedFunction_Array() {
        double[] xValues = {1.0, 10.0, 100.0};
        double[] yValues = {5.0, 10.0, 15.0};
        ArrayTabulatedFunction array = new ArrayTabulatedFunction(xValues, yValues);
        UnmodifiableTabulatedFunction unmodified = new UnmodifiableTabulatedFunction(array);

        // Проверка, что setY запрещен
        assertThrows(UnsupportedOperationException.class, () -> unmodified.setY(1, 10.0));

        // Проверка методов
        assertEquals(3, unmodified.getCount());
        assertEquals(1.0, unmodified.getX(0), 1e-10);
        assertEquals(5.0, unmodified.getY(0), 1e-10);
        assertEquals(1, unmodified.indexOfX(10.0));
        assertEquals(2, unmodified.indexOfY(15.0));
        assertEquals(1.0, unmodified.leftBound(), 1e-10);
        assertEquals(100.0, unmodified.rightBound(), 1e-10);

        // Проверка apply
        assertEquals(5.0, unmodified.apply(1.0), 1e-10);
        assertEquals(11.0, unmodified.apply(28.0), 1e-10);
        assertEquals(0.0, unmodified.apply(-8.0), 1e-10);

        // Проверка итератора
        Iterator<Point> iterator = unmodified.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }
        assertEquals(xValues.length, index);

    }
    @Test
    // Проверка всех переопределённых функций array в декораторе, который запрещает модификацию значений
    public void testUnmodifiableTabulatedFunction_List() {
        double[] xValues = {1.0, 10.0, 100.0};
        double[] yValues = {5.0, 10.0, 15.0};
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(xValues, yValues);
        UnmodifiableTabulatedFunction unmodified = new UnmodifiableTabulatedFunction(list);

        // Проверка, что setY запрещен
        assertThrows(UnsupportedOperationException.class, () -> unmodified.setY(1, 10.0));

        // Проверка методов
        assertEquals(3, unmodified.getCount());
        assertEquals(1.0, unmodified.getX(0), 1e-10);
        assertEquals(5.0, unmodified.getY(0), 1e-10);
        assertEquals(1, unmodified.indexOfX(10.0));
        assertEquals(2, unmodified.indexOfY(15.0));
        assertEquals(1.0, unmodified.leftBound(), 1e-10);
        assertEquals(100.0, unmodified.rightBound(), 1e-10);

        // Проверка apply
        assertEquals(5.0, unmodified.apply(1.0), 1e-10);
        assertEquals(11.0, unmodified.apply(28.0), 1e-10);
        assertEquals(0.0, unmodified.apply(-8.0), 1e-10);

        // Проверка итератора
        Iterator<Point> iterator = unmodified.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }
        assertEquals(xValues.length, index);

    }

    @Test
    // Проверка всех переопределённых функций array в декораторе, который запрещает модификацию значений и apply
    public void testStrictAndUnmodifiable_array() {
        double[] xValues = {1.0, 10.0, 100.0};
        double[] yValues = {5.0, 10.0, 15.0};
        ArrayTabulatedFunction array = new ArrayTabulatedFunction(xValues, yValues);
        UnmodifiableTabulatedFunction unmodified = new UnmodifiableTabulatedFunction(array);
        StrictTabulatedFunction strict = new StrictTabulatedFunction(unmodified);

        // Проверка, что apply запрещен
        assertThrows(UnsupportedOperationException.class, () -> strict.apply(1000.0));
        // Но не запрещено просто вставлять имеющиеся значения
        assertEquals(10.0, strict.apply(10.0), 1e-10);
        // Проверка, что setY запрещен
        assertThrows(UnsupportedOperationException.class, () -> strict.setY(1, 10.0));

        // Проверка методов
        assertEquals(3, strict.getCount());
        assertEquals(1.0, strict.getX(0), 1e-10);
        assertEquals(5.0, strict.getY(0), 1e-10);
        assertEquals(1, strict.indexOfX(10.0));
        assertEquals(2, strict.indexOfY(15.0));
        assertEquals(1.0, strict.leftBound(), 1e-10);
        assertEquals(100.0, strict.rightBound(), 1e-10);

        // Проверка итератора
        Iterator<Point> iterator = strict.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }
        assertEquals(xValues.length, index);

    }

    @Test
    // Проверка всех переопределённых функций list в декораторе, который запрещает модификацию значений и apply
    public void testStrictAndUnmodifiable_list() {
        double[] xValues = {1.0, 10.0, 100.0};
        double[] yValues = {5.0, 10.0, 15.0};
        LinkedListTabulatedFunction array = new LinkedListTabulatedFunction(xValues, yValues);
        UnmodifiableTabulatedFunction unmodified = new UnmodifiableTabulatedFunction(array);
        StrictTabulatedFunction strict = new StrictTabulatedFunction(unmodified);

        // Проверка, что apply запрещен
        assertThrows(UnsupportedOperationException.class, () -> strict.apply(1000.0));
        // Но не запрещено просто вставлять имеющиеся значения
        assertEquals(10.0, strict.apply(10.0), 1e-10);
        // Проверка, что setY запрещен
        assertThrows(UnsupportedOperationException.class, () -> strict.setY(1, 10.0));

        // Проверка методов
        assertEquals(3, strict.getCount());
        assertEquals(1.0, strict.getX(0), 1e-10);
        assertEquals(5.0, strict.getY(0), 1e-10);
        assertEquals(1, strict.indexOfX(10.0));
        assertEquals(2, strict.indexOfY(15.0));
        assertEquals(1.0, strict.leftBound(), 1e-10);
        assertEquals(100.0, strict.rightBound(), 1e-10);

        // Проверка итератора
        Iterator<Point> iterator = strict.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(xValues[index], point.x, 1e-10);
            assertEquals(yValues[index], point.y, 1e-10);
            index++;
        }
        assertEquals(xValues.length, index);

    }
}