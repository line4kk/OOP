package functions.factory;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionFactoryTest {

    @Test
    void create() {
        LinkedListTabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        double[] xValues = {-5.0, -3.0};
        double[] yValues = {1.0, 7.0};
        TabulatedFunction function = factory.create(xValues, yValues);

        assertInstanceOf(LinkedListTabulatedFunction.class, function);
    }
}