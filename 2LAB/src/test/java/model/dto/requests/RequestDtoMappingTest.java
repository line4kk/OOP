package model.dto.requests;

import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RequestDtoMappingTest {

    @Test
    void testUserRegisterRequestToUser() {
        UserRegisterRequest request = new UserRegisterRequest("vasya", "12345", "GOD", "tab");

        User user = request.toEntity();

        assertNotNull(user);
        assertEquals("vasya", user.getUsername());
        assertEquals("12345", user.getPasswordHash());
        assertEquals("GOD", user.getRole());
        assertEquals("tab", user.getFactoryType());
    }

    @Test
    void testFunctionCreateRequestToFunction() {
        FunctionCreateRequest request = new FunctionCreateRequest("ya ustal", "type", "sos");

        Function function = request.toEntity(5L);

        assertNotNull(function);
        assertEquals(5L, function.getUserId());
        assertEquals("ya ustal", function.getName());
        assertEquals("type", function.getType());
        assertEquals("sos", function.getSource());
    }

    @Test
    void testPointRequestToFunctionPoint() {
        PointRequest request = new PointRequest(3.0, 9.0);

        FunctionPoint point = request.toEntity(10L);

        assertNotNull(point);
        assertEquals(10L, point.getFunctionId());
        assertEquals(3.0, point.getXValue(), 0.0001);
        assertEquals(9.0, point.getYValue(), 0.0001);
    }


    @Test
    void testEmptyCompositeCreateRequest() {
        CompositeCreateRequest request = new CompositeCreateRequest();

        List<CompositeFunctionElement> elements = request.toEntities(1L);

        assertTrue(elements.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("compositeRequestSamples")
    void testCompositeCreateRequestDynamicOrdering(List<Long> ids) {
        CompositeCreateRequest request = new CompositeCreateRequest(ids);

        List<CompositeFunctionElement> elements = request.toEntities(55L);

        assertEquals(ids.size(), elements.size());
        for (int i = 0; i < ids.size(); i++) {
            CompositeFunctionElement element = elements.get(i);
            assertEquals(i + 1, element.getFunctionOrder());
            assertEquals(55L, element.getCompositeId());
            assertEquals(ids.get(i), element.getFunctionId());
        }
    }

    static Stream<Arguments> compositeRequestSamples() {
        return Stream.of(
                Arguments.of(List.of(10L, 20L, 30L)),
                Arguments.of(List.of(42L)),
                Arguments.of(List.of(3L, 2L, 1L, 0L))
        );
    }

    static Stream<Arguments> operationSamples() {
        return Stream.of(
                Arguments.of(1L, 2L, "DIVIDE", -5.5),
                Arguments.of(2L, 2L, "MULTIPLY", 0.0),
                Arguments.of(99L, 100L, "ADD", 123.456)
        );
    }
}