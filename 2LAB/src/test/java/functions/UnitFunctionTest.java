package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitFunctionTest {
    @Test
    void testAlwaysReturnZero() {
        UnitFunction one = new UnitFunction();
        final double delta = 1e-8;

        assertEquals(1.0, one.apply(0), delta);
        assertEquals(1.0, one.apply(-32), delta);
        assertEquals(1.0, one.apply(-1.0), delta);
        assertEquals(1.0, one.apply(5678.322), delta);
        assertEquals(1.0, one.apply(22.022), delta);
        assertEquals(1.0, one.apply(-9911), delta);
    }

    @Test
        // Проверка, что метод getConstant возвращает 0.0
    void testGetConstReturnsZero() {
        UnitFunction one = new UnitFunction();

        assertEquals(1.0, one.getConst(), 1e-8);
    }
    @Test
        // Проверка на наследование
    void testExtendFromConstantFunction() {
        UnitFunction one = new UnitFunction();

        // ZeroFunction является наследником ConstantFunction
        assertTrue(one instanceof ConstantFunction);

        // ZeroFunction реализует MathFunction
        assertTrue(one instanceof MathFunction);
    }
}