package engine.service;

import functions.IdentityFunction;
import functions.SqrFunction;
import functions.UnitFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticalFunctionRegistryTest {

    @Test
    void shouldExposeRegisteredAnalyticalFunctions() {
        assertTrue(AnalyticalFunctionRegistry.isAnalyticalFunction("SqrFunction"));
        assertTrue(AnalyticalFunctionRegistry.isAnalyticalFunction("UnitFunction"));
        assertTrue(AnalyticalFunctionRegistry.isAnalyticalFunction("IdentityFunction"));
    }

    @Test
    void shouldReturnFunctionInstanceByName() {
        var sqr = AnalyticalFunctionRegistry.getFunction("SqrFunction");
        assertEquals(25.0, sqr.apply(5.0));

        var unit = AnalyticalFunctionRegistry.getFunction("UnitFunction");
        assertEquals(1.0, unit.apply(123.0));

        var identity = AnalyticalFunctionRegistry.getFunction("IdentityFunction");
        assertEquals(-7.5, identity.apply(-7.5));
    }

    @Test
    void shouldReturnUnmodifiableRegisteredFunctionsMap() {
        var registered = AnalyticalFunctionRegistry.getRegisteredFunctions();
        assertTrue(registered.containsKey("SqrFunction"));
        assertThrows(UnsupportedOperationException.class,
                () -> registered.put("New", new SqrFunction()));
    }

    @Test
    void shouldFailForUnknownFunctionName() {
        assertThrows(IllegalArgumentException.class,
                () -> AnalyticalFunctionRegistry.getFunction("UnknownFunction"));
    }
}
