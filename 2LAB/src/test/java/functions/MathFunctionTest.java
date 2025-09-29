package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathFunctionTest {

    @Test
    // Проверка 2 функций
    public void testAndThenSingleComposition() {
        // f(x) = 2x
        MathFunction f = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x;
            }
        };

        // g(x) = x + 3
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 3;
            }
        };

        // g(f(x)) = (2x) + 3
        MathFunction composite = f.andThen(g);

        assertEquals(5.0, composite.apply(1.0), 1e-8);
        assertEquals(7.0, composite.apply(2.0), 1e-8);
    }

    @Test
    // Проверка 3 функций
    public void testAndThenChainThreeFunctions() {
        // f(x) = x²
        MathFunction f = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        // g(x) = x + 1
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + 1;
            }
        };

        // h(x) = 3x
        MathFunction h = new MathFunction() {
            @Override
            public double apply(double x) {
                return 3 * x;
            }
        };

        // g(f(h(x))) = (3*x)^2 + 1
        MathFunction composite = h.andThen(f).andThen(g);

        assertEquals(10.0, composite.apply(1.0), 1e-8);
        assertEquals(37.0, composite.apply(2.0), 1e-8);
    }

    @Test
    // Проверка с экспонентой и логарифмом
    public void testAndThenWithExponentialAndLogarithm() {
        // exp(x) = e^x
        MathFunction exp = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.exp(x);
            }
        };

        // log(x) = ln(x)
        MathFunction log = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.log(x);
            }
        };

        // log(exp(x)) = x
        MathFunction identity = exp.andThen(log);
        assertEquals(2.5, identity.apply(2.5), 1e-8);
        assertEquals(-40, identity.apply(-40), 1e-8);

    }
    @Test
    // Проверка с обратными функциями
    public void testAndThenWithInverseFunctions() {
        // doubleValue(x) = 2x
        MathFunction doubleValue = new MathFunction() {
            @Override
            public double apply(double x) {
                return 2 * x;
            }
        };

        // halfValue(x) = x/2
        MathFunction halfValue = new MathFunction() {
            @Override
            public double apply(double x) {
                return x / 2;
            }
        };

        // halfValue(doubleValue(x)) = (2x)/2 = x
        MathFunction identity1 = doubleValue.andThen(halfValue);
        assertEquals(5.0, identity1.apply(5.0), 1e-8);
        assertEquals(-3.0, identity1.apply(-3.0), 1e-8);
    }
}