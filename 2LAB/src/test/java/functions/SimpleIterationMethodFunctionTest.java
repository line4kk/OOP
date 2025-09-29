package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleIterationMethodFunctionTest {

    @Test
    // Уравнение: x - cos(x) = 0, корень ≈ 0.73908
    public void testCosineEquation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.cos(x);
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return -Math.sin(x);
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(0.5);

        assertEquals(0.73908, result, 1e-5, "Корень уравнения x - cos(x) = 0");
    }

    @Test
    // Уравнение: tan(x) - 5*x = 0, корень ≈ 0.0
    public void testTangentEquation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return (1.0/5) * Math.tan(x);
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return (1.0/5) * 1/(Math.pow(Math.cos(x), 2));
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(-0.5);

        assertEquals(0.0, result, 1e-5, "Корень уравнения tan(x) - 5*x = 0");
    }

    @Test
    // Уравнение: arccos(x/25) - x = 0, корень ≈ 1.51034
    public void testArcCosEquation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.acos(x/25);
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return -(1.0/25)*(1/(Math.sqrt(1 - x*x)));
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(10);

        assertEquals(1.51034, result, 1e-5, "Корень уравнения arccos(x/25) - x = 0");
    }

    @Test
    // Уравнение: (cos(x) + sin(x))/2 - x = 0, корень ≈ 0.70481
    public void testSinCosEquation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return (Math.cos(x) + Math.sin(x)) / 2;
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return (-Math.sin(x) + Math.cos(x)) / 2;
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(-5);

        assertEquals(0.70481, result, 1e-5, "Корень уравнения (cos(x) + sin(x))/2 - x = 0");
    }

    @Test
    // Уравнение: // x - (x^2 + 3)/4 = 0, корень ≈ 1.0
    public void testRoot1Equation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return (x * x + 3) / 4;
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return x / 2;
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(-0.82);

        assertEquals(1.0, result, 1e-5, "Корень уравнения x - (x^2 + 3)/4 = 0");
    }

    @Test
    // Уравнение: sqrt(x + 2) - x = 0, корень ≈ 2.0
    public void testRoot2Equation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return Math.sqrt(x + 2);
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return 1.0 / (2 * Math.sqrt(x + 2));
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(-1.5);

        assertEquals(2.0, result, 1e-5, "Корень уравнения sqrt(x + 2) - x = 0");
    }

    @Test
    // Уравнение: x = (x² + 2)/(2x + 1), корень ≈ 1.0
    public void testMultiplicationEquation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return (x * x + 2) / (2 * x + 1);
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return (2 * x * (2 * x + 1) - 2 * (x * x + 2)) / Math.pow(2 * x + 1, 2);
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(32);

        assertEquals(1.0, result, 1e-5, "Корень уравнения x = (x² + 2)/(2x + 1)");
    }
    @Test
    // Уравнение: e^(-x) - 2x = 0, корень ≈ 0.35173
    public void testExpEquation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return 0.5 * Math.exp(-x);
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return -0.5 * Math.exp(-x);
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(100);

        assertEquals(0.35173, result, 1e-5, "Корень уравнения e^(-x) - 2x = 0");
    }

    @Test
    // Уравнение: ln(x + 2) - x/3 = 0, корень ≈ 6.37617
    public void testLnEquation() {
        MathFunction g = new MathFunction() {
            @Override
            public double apply(double x) {
                return 3 * Math.log(x + 2);
            }
        };

        MathFunction dg = new MathFunction() {
            @Override
            public double apply(double x) {
                return 3.0 / (x + 2);
            }
        };

        SimpleIterationMethodFunction solver = new SimpleIterationMethodFunction(g, dg, 1e-8, 100);
        double result = solver.apply(1.5);

        assertEquals(6.37617, result, 1e-5, "Корень уравнения ln(x + 2) - x/3 = 0");
    }
}

