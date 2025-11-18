package model.dto.responses;

import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseDtoMappingTest {

    @Test
    void testUserResponseFromUser() {
        User user = new User(1L, "petya", "hash123", "USER", "tab");

        UserResponse response = UserResponse.from(user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("petya", response.getUsername());
        assertEquals("USER", response.getRole());
        assertEquals("tab", response.getFactoryType());
    }

    @Test
    void testFunctionResponseFromFunction() {
        Function function = new Function(5L, 10L, "Синус", "type", "my mind");

        FunctionResponse response = FunctionResponse.from(function);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Синус", response.getName());
        assertEquals("type", response.getType());
        assertEquals("my mind", response.getSource());
    }

    @Test
    void testPointResponseFromFunctionPoint() {
        FunctionPoint point = new FunctionPoint(7L, 3L, -2.5, 6.25);

        PointResponse response = PointResponse.from(point);

        assertNotNull(response);
        assertEquals(7L, response.getId());
        assertEquals(-2.5, response.getX(), 0.0001);
        assertEquals(6.25, response.getY(), 0.0001);
    }

    @Test
    void testOperationResultResponseFromDouble() {
        double result = 3.14;

        OperationResultResponse response = OperationResultResponse.from(result);

        assertNotNull(response);
        assertEquals(3.14, response.getResultY(), 0.0001);
    }

    @Test
    void testCompositeElementResponseFromElement() {
        CompositeFunctionElement element = new CompositeFunctionElement(15L, 999L, 2, 42L);

        CompositeElementResponse response = CompositeElementResponse.from(element);

        assertNotNull(response);
        assertEquals(15L, response.getId());
        assertEquals(2, response.getOrder());
        assertEquals(42L, response.getFunctionId());
    }
}