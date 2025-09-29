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

    double[] xValuesA1 = {0.0, 1.0, 2.0, 3.0};
    double[] yValuesA1 = {0.0, 1.0, 4.0, 9.0};
    ArrayTabulatedFunction tabFunA1 = new ArrayTabulatedFunction(xValuesA1, yValuesA1);

    double[] xValuesA2 = {1.0, 2.0, 3.0, 4.0};
    double[] yValuesA2 = {1.0, 0.5, 1.5, 2.0};
    ArrayTabulatedFunction tabFunA2 = new ArrayTabulatedFunction(xValuesA2, yValuesA2);


    double[] xValuesL1 = {0.0, 1.0, 2.0, 3.0};
    double[] yValuesL1 = {0.0, 3.0, 6.0, 9.0};
    LinkedListTabulatedFunction tabFunL1 = new LinkedListTabulatedFunction(xValuesL1, yValuesL1);

    double[] xValuesL2 = {0.0, 1.0, 2.0, 3.0};
    double[] yValuesL2 = {0.0, 0.5, 1.0, 1.5};
    LinkedListTabulatedFunction tabFunL2 = new LinkedListTabulatedFunction(xValuesL2, yValuesL2);

    @Test
    void testBothArray(){
        CompositeFunction comp1 = new CompositeFunction(tabFunA1, tabFunA2);  // A2(A1)
        CompositeFunction comp2 = new CompositeFunction(tabFunA2, tabFunA1);  // A1(A2)

        assertEquals(2.0, comp1.apply(2.0));
        assertEquals(1.0, comp2.apply(1.0));
    }

    @Test
    void testBothLinkedList(){
        CompositeFunction comp1 = new CompositeFunction(tabFunL1, tabFunL2);  // L2(L1)
        CompositeFunction comp2 = new CompositeFunction(tabFunL2, tabFunL1);  // L1(L2)

        assertEquals(6.0, comp1.apply(4));
        assertEquals(15.0, comp2.apply(10));
    }

    @Test
    void testMix(){
        CompositeFunction comp1 = new CompositeFunction(tabFunL2, tabFunA1);  // A1(L2)
        CompositeFunction comp2 = new CompositeFunction(tabFunA2, tabFunL1);  // L1(A2)

        assertEquals(2.5, comp1.apply(3));
        assertEquals(15, comp2.apply(10));
    }

    @Test
    void testTabulatedAndOther(){
        SqrFunction sqr = new SqrFunction();
        CompositeFunction comp1 = new CompositeFunction(tabFunL1, sqr);
        CompositeFunction comp2 = new CompositeFunction(sqr, tabFunL2);

        assertEquals(900, comp1.apply(10));
        assertEquals(200, comp2.apply(20));
    }
}