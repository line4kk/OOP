package Integrator;

import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import functions.ArrayTabulatedFunction;

public class ParallelIntegratorTest {

    @Test
    void testIntegrateConstantFunction() {
        double[] x = {0, 1, 2, 3};
        double[] y = {2, 2, 2, 2};
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);

        Integrator integrator = new ParallelIntegrator(100);
        double result = integrator.integrate(func);

        assertEquals(6.0, result, 0.1);
    }

    @Test
    void testIntegrateLinearFunction() {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 2};
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);

        Integrator integrator = new ParallelIntegrator(100);
        double result = integrator.integrate(func);

        assertEquals(2.0, result, 0.1);
    }

    @Test
    void testIntegrateCubeFunction() {
        double[] x = {0, 1, 2, 3, 4, 5};
        double[] y = {0, 1, 8, 27, 64, 125};
        TabulatedFunction func = new ArrayTabulatedFunction(x, y);

        Integrator integrator = new ParallelIntegrator(1000);
        double result = integrator.integrate(func);

        assertEquals(156.25, result, 10);
    }
}