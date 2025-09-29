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

    @Test
    void testCompositeOfSqr() {
        SqrFunction f = new SqrFunction();
        SqrFunction g = new SqrFunction();
        CompositeFunction h = new CompositeFunction(f, g);  // g(f(x))

        assertEquals(81, h.apply(3));
        assertEquals(625, h.apply(-5));
        assertEquals(1, h.apply(1));
        assertEquals(0, h.apply(0));
    }

    @Test
    void testCompositeOfComposites(){
        SqrFunction f = new SqrFunction();
        IdentityFunction g = new IdentityFunction();
        CompositeFunction comp1 = new CompositeFunction(f, g);  // g(f(x))

        SqrFunction h = new SqrFunction();
        CompositeFunction comp2 = new CompositeFunction(comp1, h);
        CompositeFunction compOfComps = new CompositeFunction(comp2, comp1);

        assertEquals(81, comp2.apply(3));
        assertEquals(256, compOfComps.apply(2));



    }

}