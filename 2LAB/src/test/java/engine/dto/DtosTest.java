package engine.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtosTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void functionCreateRequestShouldStoreProvidedData() {
        var request = new FunctionCreateRequest("name", "type", "source");

        assertEquals("name", request.getName());
        assertEquals("type", request.getType());
        assertEquals("source", request.getSource());

        request.setName("other");
        request.setType("new-type");
        request.setSource("new-source");

        assertEquals("other", request.getName());
        assertEquals("new-type", request.getType());
        assertEquals("new-source", request.getSource());
    }

    @Test
    void userLoginRequestShouldSupportEquality() {
        var first = new UserLoginRequest("user", "pass");
        var copy = new UserLoginRequest("user", "pass");

        assertEquals(first, copy);
        assertEquals(first.hashCode(), copy.hashCode());

        copy.setPassword("other");
        assertNotEquals(first, copy);
    }

    @Test
    void pointRequestShouldHandleNullSafeCoordinates() {
        var point = new PointRequest();

        assertNull(point.getX());
        assertNull(point.getY());

        point.setX(1.5);
        point.setY(-2.0);

        assertEquals(1.5, point.getX());
        assertEquals(-2.0, point.getY());
    }

    @Test
    void functionResponseShouldRepresentFunctionMeta() {
        var response = new FunctionResponse(5L, "f", "analytic", "x^2");

        assertEquals(5L, response.getId());
        assertEquals("f", response.getName());
        assertEquals("analytic", response.getType());
        assertEquals("x^2", response.getSource());
    }

    @Test
    void userRegisterRequestShouldExposeFactoryTypeField() throws Exception {
        var request = new UserRegisterRequest("demo", "pwd", "ROLE_USER", "array");

        assertEquals("demo", request.getUsername());
        assertEquals("pwd", request.getPassword());
        assertEquals("ROLE_USER", request.getRole());
        assertEquals("array", request.getFactory_type());

        String json = objectMapper.writeValueAsString(request);
        JsonNode node = objectMapper.readTree(json);
        assertEquals("array", node.get("factory_type").asText());
    }

    @Test
    void functionSamplingRequestShouldCaptureRangeAndCount() {
        var request = new FunctionSamplingRequest();
        request.setFunction_id(10L);
        request.setAnalytical_function_id(2L);
        request.setX_from(-1.0);
        request.setX_to(3.5);
        request.setCount(11);

        assertEquals(10L, request.getFunction_id());
        assertEquals(2L, request.getAnalytical_function_id());
        assertEquals(-1.0, request.getX_from());
        assertEquals(3.5, request.getX_to());
        assertEquals(11, request.getCount());
    }

    @Test
    void compositeCreateRequestShouldPreserveFunctionOrder() {
        var request = new CompositeCreateRequest(List.of(1L, 3L, 2L), "combo");

        assertEquals(List.of(1L, 3L, 2L), request.getFunctionIdsInOrder());
        assertEquals("combo", request.getName());
    }

    @Test
    void operationRequestShouldStoreOperandsAndOperation() {
        var request = new OperationRequest();
        request.setFunction1_id(4L);
        request.setFunction2_id(5L);
        request.setOperation("multiply");

        assertEquals(4L, request.getFunction1_id());
        assertEquals(5L, request.getFunction2_id());
        assertEquals("multiply", request.getOperation());
    }

    @Test
    void userResponseShouldRoundTripFactoryTypeJson() throws Exception {
        var response = new UserResponse(1L, "demo", "ADMIN", "linked");

        assertEquals(1L, response.getId());
        assertEquals("demo", response.getUsername());
        assertEquals("ADMIN", response.getRole());
        assertEquals("linked", response.getFactoryType());

        String json = objectMapper.writeValueAsString(response);
        JsonNode node = objectMapper.readTree(json);
        assertEquals("linked", node.get("factory_type").asText());

        UserResponse deserialized = objectMapper.readValue(json, UserResponse.class);
        assertEquals(response, deserialized);
    }

    @Test
    void functionsResponseShouldWrapFunctionList() {
        var payload = List.of(new FunctionResponse(1L, "f", "type", "src"));
        var response = new FunctionsResponse(payload);

        assertEquals(payload, response.getFunctions());

        response.setFunctions(List.of());
        assertTrue(response.getFunctions().isEmpty());
    }

    @Test
    void pointResponseShouldCarryCoordinates() {
        var response = new PointResponse(7L, -2.5, 8.0);

        assertEquals(7L, response.getId());
        assertEquals(-2.5, response.getX());
        assertEquals(8.0, response.getY());
    }

    @Test
    void compositeElementResponseShouldSerializeSnakeCaseFunctionId() throws Exception {
        var response = new CompositeElementResponse(9L, 2, 15L);

        String json = objectMapper.writeValueAsString(response);
        JsonNode node = objectMapper.readTree(json);

        assertEquals(2, node.get("order").asInt());
        assertEquals(15L, node.get("function_id").asLong());
    }
}