package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionTest {
    double[] xValues = {0.0, 1.0, 2.0, 3.0};
    double[] yValues = {0.0, 1.0, 2.0, 3.0};
    SqrFunction sqr = new SqrFunction();

    LinkedListTabulatedFunction fun1 = new LinkedListTabulatedFunction(xValues, yValues);
    LinkedListTabulatedFunction fun2 = new LinkedListTabulatedFunction(sqr, -3.0, 3.0, 7);


    @Test
    void getCount() {
        assertEquals(4, fun1.getCount());
        assertEquals(7, fun2.getCount());
    }

    @Test
    void leftBound() {
        assertEquals(0.0, fun1.leftBound());
        assertEquals(-3.0, fun2.leftBound());
    }

    @Test
    void rightBound() {
        assertEquals(3.0, fun1.rightBound());
        assertEquals(3.0, fun2.rightBound());
    }

    @Test
    void getX() {
        assertEquals(0.0, fun1.getX(0));
        assertEquals(2.0, fun1.getX(2));
        assertEquals(-3.0, fun2.getX(0));
        assertEquals(-1.0, fun2.getX(2));
    }

    @Test
    void getY() {
        assertEquals(0.0, fun1.getY(0));
        assertEquals(2.0, fun1.getY(2));
        assertEquals(9.0, fun2.getY(0));
        assertEquals(1.0, fun2.getY(2));
    }

    @Test
    void setY() {
        fun2.setY(3, 2);
        assertEquals(2, fun2.getY(3));
    }

    @Test
    void indexOfX() {
        assertEquals(2, fun1.indexOfX(2.0));
        assertEquals(5, fun2.indexOfX(2.0));
    }

    @Test
    void indexOfY() {
        assertEquals(2, fun1.indexOfY(2.0));
        assertEquals(1, fun2.indexOfY(4));
    }

    @Test
    void floorIndexOfX() {
        assertEquals(0, fun1.floorIndexOfX(0.5));
        assertEquals(4, fun2.floorIndexOfX(1.5));
    }

    @Test
    void interpolate() {
        assertEquals(0.5, fun1.interpolate(0.5, 0));
        assertEquals(0.5, fun1.interpolate(0.5, 0));
    }

    @Test
    void extrapolateLeft() {
        assertEquals(-1, fun1.interpolate(-1, 0));
        assertEquals(14, fun2.interpolate(-4, 0));
    }

    @Test
    void extrapolateRight() {
        assertEquals(7, fun1.interpolate(7, 3));
        assertEquals(14, fun2.interpolate(4, fun2.getCount()-2));
    }
}