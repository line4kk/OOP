package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompositeFunctionTest {

    @Test
    void testCompositeOfIndentity() {  // Обе функции Indentity
        IdentityFunction f = new IdentityFunction();
        IdentityFunction g = new IdentityFunction();
        CompositeFunction h = new CompositeFunction(f, g);  // g(f(x))
        CompositeFunction m = new CompositeFunction(g, f);  // f(g(x))
        CompositeFunction n = new CompositeFunction(f, f);  // f(f(x))
        CompositeFunction p = new CompositeFunction(g, g);  // f(g(x))

        assertEquals(5, h.apply(5));
        assertEquals(5, m.apply(5));
        assertEquals(5, n.apply(5));
        assertEquals(5, p.apply(5));
    }
}