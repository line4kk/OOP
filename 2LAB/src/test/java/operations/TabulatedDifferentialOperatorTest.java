package operations;

import concurrent.SynchronizedTabulatedFunction;
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

    @Test
        // Синхронизация с Array
    void testDeriveSynchronouslyWithArrayFunction() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        TabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.deriveSynchronously(function);

        assertEquals(4, derivative.getCount());
        assertEquals(1.0, derivative.getY(0), 1e-10);
        assertEquals(3.0, derivative.getY(1), 1e-10);
        assertEquals(5.0, derivative.getY(2), 1e-10);
    }

    @Test
    // Синхронизация с LinkedList
    void testDeriveSynchronouslyWithLinkedListFunction() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {3.0, 5.0, 7.0};
        TabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.deriveSynchronously(function);

        assertEquals(3, derivative.getCount());
        assertEquals(2.0, derivative.getY(0), 1e-10);
        assertEquals(2.0, derivative.getY(1), 1e-10);
    }

    @Test
    // Синхронизовання
    void testDeriveSynchronouslyWithAlreadySynchronized() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {10.0, 50.0, 100.0};
        TabulatedFunction baseFunction = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunction = new SynchronizedTabulatedFunction(baseFunction);

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction derivative = operator.deriveSynchronously(syncFunction);

        assertEquals(3, derivative.getCount());
        assertEquals(40.0, derivative.getY(0), 1e-10);
        assertEquals(50.0, derivative.getY(1), 1e-10);
    }
}