package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantFunctionTest {
    @Test
    void TestPositiveIntConstant() {
        // всегда возвращает 24
        ConstantFunction constFunc = new ConstantFunction(24);

        assertEquals(24, constFunc.apply(0));
        assertEquals(24, constFunc.apply(-210));
        assertEquals(24, constFunc.apply(0.001));
        assertEquals(24, constFunc.apply(24));
    }

    @Test
    void TestNegativeIntConstant() {
        // всегда возвращает -99
        ConstantFunction constFunc = new ConstantFunction(-99);

        assertEquals(-99, constFunc.apply(343));
        assertEquals(-99, constFunc.apply(-2345));
        assertEquals(-99, constFunc.apply(123.66));
        assertEquals(-99, constFunc.apply(-0.3332));
    }

    @Test
    void TestPositiveDoubleConstant() {
        // всегда возвращает 19.45
        ConstantFunction constFunc = new ConstantFunction(19.45);
        final double delta = 1e-8;

        assertEquals(19.45, constFunc.apply(56), delta);
        assertEquals(19.45, constFunc.apply(134.33), delta);
        assertEquals(19.45, constFunc.apply(-2), delta);
        assertEquals(19.45, constFunc.apply(-0.1932), delta);
    }

    @Test
    void TestNegativeDoubleConstant() {
        // всегда возвращает -0.5
        ConstantFunction constFunc = new ConstantFunction(-0.5);
        final double delta = 1e-8;

        assertEquals(-0.5, constFunc.apply(-312.1), delta);
        assertEquals(-0.5, constFunc.apply(-678), delta);
        assertEquals(-0.5, constFunc.apply(0), delta);
        assertEquals(-0.5, constFunc.apply(100.001), delta);
    }
    @Test
    // Проверка метода getConst
    void testGetConstantMethod() {
        final double delta = 1e-8;
        ConstantFunction f1 = new ConstantFunction(56.99);
        ConstantFunction f2 = new ConstantFunction(0);
        ConstantFunction f3 = new ConstantFunction(-0.111);
        ConstantFunction f4 = new ConstantFunction(654);

        assertEquals(56.99, f1.getConst(), delta);
        assertEquals(0, f2.getConst(), delta);
        assertEquals(-0.111, f3.getConst(), delta);
        assertEquals(654, f4.getConst(), delta);
    }

}