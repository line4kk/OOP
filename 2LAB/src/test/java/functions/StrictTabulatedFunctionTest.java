package functions;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class StrictTabulatedFunctionTest {

    @Test
    // Проверка всех переопределённых функций array в декораторе, который запрещает модификацию значений
    public void testStrictTabulatedFunction_Array() {
        double[] xValues = {1.0, 10.0, 100.0};
        double[] yValues = {5.0, 10.0, 15.0};
        ArrayTabulatedFunction array = new ArrayTabulatedFunction(xValues, yValues);
        StrictTabulatedFunction strict = new StrictTabulatedFunction(array);

        // Проверка, что apply запрещен
        assertThrows(UnsupportedOperationException.class, () -> strict.apply(1000.0));
        // Но не запрещено просто вставлять имеющиеся значения
        assertEquals(10.0, strict.apply(10.0), 1e-10);

        // Проверка методов
        assertEquals(3, strict.getCount());
        assertEquals(1.0, strict.getX(0), 1e-10);
        assertEquals(5.0, strict.getY(0), 1e-10);
        strict.setY(0, 0.0);
        assertEquals(0.0, strict.getY(0), 1e-10);
        strict.setY(0, 5.0);
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
    // Проверка всех переопределённых функций array в декораторе, который запрещает модификацию значений
    public void testStrictTabulatedFunction_List() {
        double[] xValues = {1.0, 10.0, 100.0};
        double[] yValues = {5.0, 10.0, 15.0};
        LinkedListTabulatedFunction array = new LinkedListTabulatedFunction(xValues, yValues);
        StrictTabulatedFunction strict = new StrictTabulatedFunction(array);

        // Проверка, что apply запрещен
        assertThrows(UnsupportedOperationException.class, () -> strict.apply(1000.0));
        // Но не запрещено просто вставлять имеющиеся значения
        assertEquals(10.0, strict.apply(10.0), 1e-10);

        // Проверка методов
        assertEquals(3, strict.getCount());
        assertEquals(1.0, strict.getX(0), 1e-10);
        assertEquals(5.0, strict.getY(0), 1e-10);
        strict.setY(0, 0.0);
        assertEquals(0.0, strict.getY(0), 1e-10);
        strict.setY(0, 5.0);
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