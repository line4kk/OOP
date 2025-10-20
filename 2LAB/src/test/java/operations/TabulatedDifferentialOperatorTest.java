package operations;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedDifferentialOperatorTest {

    @Test
    void testDefaultConstructor() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        assertNotNull(operator.getFactory());
        assertTrue(operator.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    void testConstructorWithFactory() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
        assertSame(factory, operator.getFactory());
    }

    @Test
    void testGetFactory() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
        assertSame(factory, operator.getFactory());
    }

    @Test
    void testSetFactory() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();

        operator.setFactory(newFactory);

        assertSame(newFactory, operator.getFactory());
    }
    @Test
    void testDeriveArray() {
        ArrayTabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);

        double[] xValues = {0.0, 1.0, 2.0, 4.0};  // y = 3x + 1; y' = 3
        double[] yValues = {1.0, 4.0, 7.0, 13.0};

        ArrayTabulatedFunction fun = new ArrayTabulatedFunction(xValues, yValues);

        TabulatedFunction derivativeFun = operator.derive(fun);

        assertEquals(0.0, derivativeFun.getX(0), 1e-10);
        assertEquals(1.0, derivativeFun.getX(1), 1e-10);
        assertEquals(2.0, derivativeFun.getX(2), 1e-10);
        assertEquals(4.0, derivativeFun.getX(3), 1e-10);

        assertEquals(3.0, derivativeFun.getY(0), 1e-10);
        assertEquals(3.0, derivativeFun.getY(1), 1e-10);
        assertEquals(3.0, derivativeFun.getY(2), 1e-10);
        assertEquals(3.0, derivativeFun.getY(3), 1e-10);

    }

    @Test
    void testDeriveLinkedList() {
        LinkedListTabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);

        double[] xValues = {0.0, 1.0, 2.0, 5.0};  // y = 77x; y' = 77
        double[] yValues = {0.0, 77.0, 154.0, 385.0};

        LinkedListTabulatedFunction fun = new LinkedListTabulatedFunction(xValues, yValues);

        TabulatedFunction derivativeFun = operator.derive(fun);

        assertEquals(0.0, derivativeFun.getX(0), 1e-10);
        assertEquals(1.0, derivativeFun.getX(1), 1e-10);
        assertEquals(2.0, derivativeFun.getX(2), 1e-10);
        assertEquals(5.0, derivativeFun.getX(3), 1e-10);

        assertEquals(77.0, derivativeFun.getY(0), 1e-10);
        assertEquals(77.0, derivativeFun.getY(1), 1e-10);
        assertEquals(77.0, derivativeFun.getY(2), 1e-10);
        assertEquals(77.0, derivativeFun.getY(3), 1e-10);

    }
}