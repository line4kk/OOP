package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SqrFunctionTest {
    SqrFunction function = new SqrFunction();

    @Test
    void applyPositiveInt() {
        assertEquals(0, function.apply(0));
        assertEquals(1, function.apply(1));
        assertEquals(4, function.apply(2));
        assertEquals(10000, function.apply(100));
        assertEquals(225, function.apply(15));
        assertEquals(2328676, function.apply(1526));
        assertEquals(1335683209, function.apply(36547));
    }
    @Test
    void applyNegativeInt() {
        assertEquals(0, function.apply(-0));
        assertEquals(1, function.apply(-1));
        assertEquals(4, function.apply(-2));
        assertEquals(10000, function.apply(-100));
        assertEquals(225, function.apply(-15));
        assertEquals(2328676, function.apply(-1526));
        assertEquals(1335683209, function.apply(-36547));
    }
    @Test
    void applyPositiveDouble() {
        final double delta = 0.000001;
        assertEquals(0.01, function.apply(0.1), delta);
        assertEquals(42.25, function.apply(6.5), delta);
        assertEquals(10060.09, function.apply(100.3), delta);
        assertEquals(0.007921, function.apply(0.089), delta);
        assertEquals(670.188544, function.apply(25.888), delta);
        assertEquals(3589.2081, function.apply(59.91), delta);
    }
    @Test
    void applyNegativeDouble() {
        final double delta = 0.000001;
        assertEquals(0.01, function.apply(-0.1), delta);
        assertEquals(42.25, function.apply(-6.5), delta);
        assertEquals(10060.09, function.apply(-100.3), delta);
        assertEquals(0.007921, function.apply(-0.089), delta);
        assertEquals(670.188544, function.apply(-25.888), delta);
        assertEquals(3589.2081, function.apply(-59.91), delta);
    }
}