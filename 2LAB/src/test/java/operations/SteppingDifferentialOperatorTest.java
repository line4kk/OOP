package operations;

import functions.ConstantFunction;
import functions.IdentityFunction;
import functions.MathFunction;
import functions.SqrFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SteppingDifferentialOperatorTest {

    @Test
    public void testSteppingDifferentialOperatorConstructorValidStep() {
        SteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(0.1);
        assertEquals(0.1, operator.getStep(), 1e-10);
    }

    @Test
    public void testSteppingDifferentialOperatorConstructorInvalidStep() {
        // Отрицательный шаг
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(-0.1));

        // Нулевой шаг
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(0.0));

        // Бесконечность
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(Double.POSITIVE_INFINITY));

        // NaN
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(Double.NaN));
    }

    @Test
    public void testSetStepValid() {
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(0.1);
        operator.setStep(0.5);
        assertEquals(0.5, operator.getStep(), 1e-10);
    }

    @Test
    public void testSetStepInvalid() {
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(0.1);

        assertThrows(IllegalArgumentException.class, () -> operator.setStep(-1.0));
        assertThrows(IllegalArgumentException.class, () -> operator.setStep(0.0));
        assertThrows(IllegalArgumentException.class, () -> operator.setStep(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> operator.setStep(Double.NaN));
    }

    @Test
    public void testLeftSteppingDifferentialOperatorWithSqrFunction() {
        // Для f(x) = x² производная f'(x) = 2x
        // Левая разностная производная: (f(x) - f(x-h))/h
        double step = 0.1;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        SqrFunction sqrFunction = new SqrFunction();

        MathFunction derivative = operator.derive(sqrFunction);

        // Проверяем в точке x=2.0
        double result = derivative.apply(2.0);
        assertEquals(3.9, result, 1e-10);

        // Проверяем в точке x=0.0
        double resultAtZero = derivative.apply(0.0);
        assertEquals(-0.1, resultAtZero, 1e-10);
    }

    @Test
    public void testRightSteppingDifferentialOperatorWithSqrFunction() {
        // Для f(x) = x² производная f'(x) = 2x
        // Правая разностная производная: (f(x+h) - f(x))/h
        double step = 0.1;
        RightSteppingDifferentialOperator operator = new RightSteppingDifferentialOperator(step);
        SqrFunction sqrFunction = new SqrFunction();

        MathFunction derivative = operator.derive(sqrFunction);

        // Проверяем в точке x=2.0
        double result = derivative.apply(2.0);
        assertEquals(4.1, result, 1e-10);

        // Проверяем в точке x=0.0
        double resultAtZero = derivative.apply(0.0);
        assertEquals(0.1, resultAtZero, 1e-10);
    }

    @Test
    public void testMiddleSteppingDifferentialOperatorWithSqrFunction() {
        // Для f(x) = x² производная f'(x) = 2x
        double step = 0.1;
        MiddleSteppingDifferentialOperator operator = new MiddleSteppingDifferentialOperator(step);
        SqrFunction sqrFunction = new SqrFunction();

        MathFunction derivative = operator.derive(sqrFunction);

        // Проверяем в точке x=2.0
        double result = derivative.apply(2.0);
        assertEquals(4.0, result, 1e-10);

        // Проверяем в точке x=0.0
        double resultAtZero = derivative.apply(0.0);
        assertEquals(0.0, resultAtZero, 1e-10);
    }

    @Test
    public void testLeftSteppingDifferentialOperatorWithIdentityFunction() {
        // Для f(x) = x производная f'(x) = 1
        double step = 0.1;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        IdentityFunction identityFunction = new IdentityFunction();

        MathFunction derivative = operator.derive(identityFunction);

        // (x - (x-h)) / h = h / h = 1
        assertEquals(1.0, derivative.apply(5.0), 1e-10);
        assertEquals(1.0, derivative.apply(-3.0), 1e-10);
        assertEquals(1.0, derivative.apply(0.0), 1e-10);
    }

    @Test
    public void testRightSteppingDifferentialOperatorWithIdentityFunction() {
        // Для f(x) = x производная f'(x) = 1
        double step = 0.1;
        RightSteppingDifferentialOperator operator = new RightSteppingDifferentialOperator(step);
        IdentityFunction identityFunction = new IdentityFunction();

        MathFunction derivative = operator.derive(identityFunction);

        // ((x+h) - x) / h = h / h = 1
        assertEquals(1.0, derivative.apply(5.0), 1e-10);
        assertEquals(1.0, derivative.apply(-3.0), 1e-10);
        assertEquals(1.0, derivative.apply(0.0), 1e-10);
    }

    @Test
    public void testMiddleSteppingDifferentialOperatorWithIdentityFunction() {
        // Для f(x) = x производная f'(x) = 1
        double step = 0.1;
        MiddleSteppingDifferentialOperator operator = new MiddleSteppingDifferentialOperator(step);
        IdentityFunction identityFunction = new IdentityFunction();

        MathFunction derivative = operator.derive(identityFunction);

        // ((x+h) - (x-h)) / (2h) = (2h) / (2h) = 1
        assertEquals(1.0, derivative.apply(5.0), 1e-10);
        assertEquals(1.0, derivative.apply(-3.0), 1e-10);
        assertEquals(1.0, derivative.apply(0.0), 1e-10);
    }

    @Test
    public void testLeftSteppingDifferentialOperatorWithConstantFunction() {
        // Для f(x) = C производная f'(x) = 0
        double step = 0.1;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        ConstantFunction constantFunction = new ConstantFunction(5.0);

        MathFunction derivative = operator.derive(constantFunction);

        // (C - C) / h = 0
        assertEquals(0.0, derivative.apply(5.0), 1e-10);
        assertEquals(0.0, derivative.apply(-3.0), 1e-10);
        assertEquals(0.0, derivative.apply(0.0), 1e-10);
    }

    @Test
    public void testDifferentStepSizes() {
        SqrFunction sqrFunction = new SqrFunction();

        // Тестируем с разными шагами
        double[] steps = {0.01, 0.05, 0.1, 0.5};

        for (double step : steps) {
            MiddleSteppingDifferentialOperator operator = new MiddleSteppingDifferentialOperator(step);
            MathFunction derivative = operator.derive(sqrFunction);

            // Для f(x) = x² точная производная в точке x=2.0 равна 4.0
            double result = derivative.apply(2.0);
            double exact = 4.0;
            double error = Math.abs(result - exact);

            // Меньший шаг должен давать меньшую ошибку
            assertTrue(error < 0.1);
        }
    }

    @Test
    public void testDerivativeIsFunction() {
        // Проверяем, что производная действительно является функцией
        double step = 0.1;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        SqrFunction sqrFunction = new SqrFunction();

        MathFunction derivative = operator.derive(sqrFunction);

        // Должна быть возможность вызывать apply с разными аргументами
        double result1 = derivative.apply(1.0);
        double result2 = derivative.apply(2.0);
        double result3 = derivative.apply(3.0);

        assertTrue(Double.isFinite(result1));
        assertTrue(Double.isFinite(result2));
        assertTrue(Double.isFinite(result3));

        // Для квадратичной функции производная должна быть линейной
        // f(x) = x², f'(x) ≈ 2x
        assertTrue(result3 > result2);
        assertTrue(result2 > result1);
    }

}