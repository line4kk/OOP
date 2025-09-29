package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZeroFunctionTest {
    @Test
    void testAlwaysReturnZero() {
        ZeroFunction zero = new ZeroFunction();
        final double delta = 1e-8;

        assertEquals(0.0, zero.apply(0), delta);
        assertEquals(0.0, zero.apply(-32), delta);
        assertEquals(0.0, zero.apply(-1.0), delta);
        assertEquals(0.0, zero.apply(5678.322), delta);
        assertEquals(0.0, zero.apply(22.022), delta);
        assertEquals(0.0, zero.apply(-9911), delta);
    }

    @Test
        // Проверка, что метод getConstant возвращает 0.0
    void testGetConstReturnsZero() {
        ZeroFunction zero = new ZeroFunction();

        assertEquals(0.0, zero.getConst(), 1e-8);
    }
    @Test
    // Проверка на наследование
    void testExtendFromConstantFunction() {
        ZeroFunction zero = new ZeroFunction();

        // ZeroFunction является наследником ConstantFunction
        assertTrue(zero instanceof ConstantFunction);

        // ZeroFunction реализует MathFunction
        assertTrue(zero instanceof MathFunction);
    }
}