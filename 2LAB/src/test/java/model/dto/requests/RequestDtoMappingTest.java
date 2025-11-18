package model.dto.requests;

import model.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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
    void testOperationRequestToOperationResultPoint() {
        OperationRequest request = new OperationRequest(1L, 2L, "ADD");

        OperationResultPoint result = request.toEntity(6.0);

        assertNotNull(result);
        assertEquals(1L, result.getPoint1Id());
        assertEquals(2L, result.getPoint2Id());
        assertEquals("ADD", result.getOperation());
        assertEquals(6.0, result.getResultY(), 0.0001);
    }

    @Test
    void testCompositeCreateRequestToElements() {
        List<Long> ids = Arrays.asList(100L, 200L, 300L);
        CompositeCreateRequest request = new CompositeCreateRequest(ids);

        List<CompositeFunctionElement> elements = request.toEntities(777L);

        assertEquals(3, elements.size());

        CompositeFunctionElement first = elements.get(0);
        assertEquals(777L, first.getCompositeId());
        assertEquals(1, first.getFunctionOrder());
        assertEquals(100L, first.getFunctionId());

        CompositeFunctionElement second = elements.get(1);
        assertEquals(2, second.getFunctionOrder());
        assertEquals(200L, second.getFunctionId());

        CompositeFunctionElement third = elements.get(2);
        assertEquals(3, third.getFunctionOrder());
        assertEquals(300L, third.getFunctionId());
    }

    @Test
    void testEmptyCompositeCreateRequest() {
        CompositeCreateRequest request = new CompositeCreateRequest();

        List<CompositeFunctionElement> elements = request.toEntities(1L);

        assertTrue(elements.isEmpty());
    }
}