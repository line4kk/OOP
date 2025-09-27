package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityFunctionTest {
    IdentityFunction function = new IdentityFunction();

    @Test
    void applyTestInteger() {  // Проверка с целыми числами
        assertEquals(863, function.apply(863));
        assertEquals(3, function.apply(3));
        assertEquals(-947, function.apply(-947));
        assertEquals(76, function.apply(76));
        assertEquals(-518, function.apply(-518));
        assertEquals(0, function.apply(0));
    }

    @Test
    void applyTestDouble() {  // Проверка с дробными числами с погрешностью 0.000001
        final double delta = 0.000001;
        assertEquals(67.916, function.apply(67.916), delta);
        assertEquals(3.14, function.apply(3.14), delta);
        assertEquals(83.99572168, function.apply(83.99572168), delta);
        assertEquals(-668.61, function.apply(-668.61), delta);
        assertEquals(-9681.5186, function.apply(-9681.5186), delta);
    }

    @Test
    void applyTestLargeNumbers(){
        assertEquals(4975387, function.apply(4975387));
        assertEquals(-56784165, function.apply(-56784165));
        assertEquals(514145.953618, function.apply(514145.953618));
        assertEquals(-76315.4151, function.apply(-76315.4151));
        assertEquals(100000000, function.apply(100000000));
    }
}
