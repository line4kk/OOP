package functions.factory;

import functions.ArrayTabulatedFunction;
import functions.StrictTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnmodifiableTabulatedFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayTabulatedFunctionFactoryTest {

    @Test
    void create() {
        ArrayTabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        double[] xValues = {-5.0, -3.0};
        double[] yValues = {1.0, 7.0};
        TabulatedFunction function = factory.create(xValues, yValues);

        assertInstanceOf(ArrayTabulatedFunction.class, function);
    }

    @Test
    public void testArrayFactoryCreateStrict() {

        double[] xValues = {0.0, 300.0, 1300.0};
        double[] yValues = {22.0, 202.0, 2002.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction strictFunction = factory.createStrict(xValues, yValues);

        assertTrue(strictFunction instanceof StrictTabulatedFunction);
        assertThrows(UnsupportedOperationException.class, () -> strictFunction.apply(1.5));
    }

    @Test
    public void testArrayFactoryCreateUnmodified() {

        double[] xValues = {0.0, 300.0, 1300.0};
        double[] yValues = {22.0, 202.0, 2002.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction unmodFunction = factory.createUnmodifiable(xValues, yValues);

        assertTrue(unmodFunction instanceof UnmodifiableTabulatedFunction);
        assertThrows(UnsupportedOperationException.class, () -> unmodFunction.setY(0, 100.0));
    }

    @Test
    public void testArrayFactoryCreateUnmodifiedStrict() {

        double[] xValues = {0.0, 300.0, 1300.0};
        double[] yValues = {22.0, 202.0, 2002.0};

        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction bothFunction = factory.createUnmodifiableStrict(xValues, yValues);

        assertThrows(UnsupportedOperationException.class, () -> bothFunction.setY(0, 100.0));
        assertThrows(UnsupportedOperationException.class, () -> bothFunction.apply(1.5));
    }
}