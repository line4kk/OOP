package model.dto.responses;

import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("userResponseSamples")
    void testUserResponseDataVariations(long id, String username, String role, String factoryType) {
        User user = new User(id, username, null, role, factoryType);

        UserResponse response = UserResponse.from(user);

        assertEquals(id, response.getId());
        assertEquals(username, response.getUsername());
        assertEquals(role, response.getRole());
        assertEquals(factoryType, response.getFactoryType());
    }

    @ParameterizedTest
    @MethodSource("functionResponseSamples")
    void testFunctionResponseVariations(Function function) {
        FunctionResponse response = FunctionResponse.from(function);

        assertEquals(function.getId(), response.getId());
        assertEquals(function.getName(), response.getName());
        assertEquals(function.getType(), response.getType());
        assertEquals(function.getSource(), response.getSource());
    }

    static Stream<Arguments> userResponseSamples() {
        return Stream.of(
                Arguments.of(1L, "alpha", "USER", "factory1"),
                Arguments.of(5L, "beta", "ADMIN", "factory2"),
                Arguments.of(10L, "gamma", "POWER", "factory3")
        );
    }

    static Stream<Function> functionResponseSamples() {
        return Stream.of(
                new Function(5L, 10L, "Синус", "type", "my mind"),
                new Function(7L, 20L, "Exp", "analytic", "generated"),
                new Function(9L, 30L, "Poly", "tab", "upload")
        );
    }
}