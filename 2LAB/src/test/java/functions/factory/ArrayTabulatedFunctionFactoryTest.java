package functions.factory;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
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
}