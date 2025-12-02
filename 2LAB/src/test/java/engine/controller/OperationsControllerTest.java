package engine.controller;

import engine.dto.OperationRequest;
import engine.dto.PointResponse;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import engine.service.MultipleSearchService;
import engine.service.SingleSearchService;
import engine.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationsControllerTest {

    @Mock
    private MultipleSearchService multipleSearchService;
    @Mock
    private SingleSearchService singleSearchService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private OperationsController controller;

    private Users currentUser;
    private Functions baseFunction;
    private List<FunctionPoints> basePoints;

    @BeforeEach
    void setUp() {
        currentUser = new Users();
        currentUser.setId(1L);
        currentUser.setUsername("owner");

        baseFunction = new Functions();
        baseFunction.setId(10L);
        baseFunction.setUser(currentUser);
        baseFunction.setName("f1");
        baseFunction.setType("array_tabulated");

        basePoints = Arrays.asList(
                new FunctionPoints(baseFunction, 0.0, 0.0),
                new FunctionPoints(baseFunction, 1.0, 1.0),
                new FunctionPoints(baseFunction, 2.0, 4.0)
        );
    }

    @Test
    void returnsForbiddenWhenNoAuthenticatedUser() {
        when(securityUtils.getCurrentUser()).thenReturn(null);

        OperationRequest request = new OperationRequest();
        request.setOperation("derive");
        request.setFunction1_id(1L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Forbidden", response.getBody());
    }

    @Test
    void returnsNotFoundForUnknownOperation() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);

        OperationRequest request = new OperationRequest();
        request.setOperation("unknown");
        request.setFunction1_id(1L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Неизвестная операция", response.getBody());
    }

    @Test
    void validatesFunction1Presence() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);

        OperationRequest request = new OperationRequest();
        request.setOperation("derive");

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("function1_id"));
    }

    @Test
    void returnsNotFoundWhenFunction1Missing() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.empty());

        OperationRequest request = new OperationRequest();
        request.setOperation("derive");
        request.setFunction1_id(10L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("10"));
    }

    @Test
    void returnsForbiddenWhenUserCannotAccessFunction() {
        Users anotherUser = new Users();
        anotherUser.setId(2L);

        baseFunction.setUser(anotherUser);
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(baseFunction));
        when(securityUtils.canAccessUserData(anotherUser)).thenReturn(false);

        OperationRequest request = new OperationRequest();
        request.setOperation("derive");
        request.setFunction1_id(10L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Forbidden", response.getBody());
    }

    @Test
    void rejectsNonTabulatedFunction() {
        baseFunction.setType("analytic");
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(baseFunction));
        when(securityUtils.canAccessUserData(currentUser)).thenReturn(true);

        OperationRequest request = new OperationRequest();
        request.setOperation("derive");
        request.setFunction1_id(10L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("табулированных"));
    }

    @Test
    void returnsNotFoundWhenFunctionHasNoPoints() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(baseFunction));
        when(securityUtils.canAccessUserData(currentUser)).thenReturn(true);
        when(multipleSearchService.findPointsByFunction(10L)).thenReturn(Collections.emptyList());

        OperationRequest request = new OperationRequest();
        request.setOperation("derive");
        request.setFunction1_id(10L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("не имеет точек"));
    }

    @Test
    void performsDerivativeOperation() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(baseFunction));
        when(securityUtils.canAccessUserData(currentUser)).thenReturn(true);
        when(multipleSearchService.findPointsByFunction(10L)).thenReturn(basePoints);

        Functions derivative = new Functions();
        derivative.setId(20L);
        derivative.setUser(currentUser);
        derivative.setName("f1_derivative");
        derivative.setType("array_tabulated");
        when(singleSearchService.saveFunction(any(Functions.class))).thenReturn(derivative);

        List<FunctionPoints> derivativePoints = Arrays.asList(
                new FunctionPoints(derivative, 0.0, 1.0),
                new FunctionPoints(derivative, 1.0, 2.0)
        );
        when(multipleSearchService.findPointsByFunction(20L)).thenReturn(derivativePoints);

        OperationRequest request = new OperationRequest();
        request.setOperation("derive");
        request.setFunction1_id(10L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(200, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        List<PointResponse> body = (List<PointResponse>) response.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());
        verify(singleSearchService, atLeastOnce()).saveFunctionPoint(any(FunctionPoints.class));
    }

    @Test
    void requiresSecondFunctionForBinaryOperations() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(baseFunction));
        when(securityUtils.canAccessUserData(currentUser)).thenReturn(true);
        when(multipleSearchService.findPointsByFunction(10L)).thenReturn(basePoints);

        OperationRequest request = new OperationRequest();
        request.setOperation("add");
        request.setFunction1_id(10L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("function2_id"));
    }

    @Test
    void returns418WhenDividingByFunctionWithZeroPoint() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(baseFunction));
        when(securityUtils.canAccessUserData(currentUser)).thenReturn(true);
        when(multipleSearchService.findPointsByFunction(10L)).thenReturn(basePoints);

        Functions function2 = new Functions();
        function2.setId(11L);
        function2.setUser(currentUser);
        function2.setName("g");
        function2.setType("array_tabulated");
        when(singleSearchService.findFunctionById(11L)).thenReturn(Optional.of(function2));
        when(securityUtils.canAccessUserData(function2.getUser())).thenReturn(true);

        List<FunctionPoints> function2Points = Arrays.asList(
                new FunctionPoints(function2, 0.0, 0.0),
                new FunctionPoints(function2, 1.0, 0.0)
        );
        when(multipleSearchService.findPointsByFunction(11L)).thenReturn(function2Points);

        OperationRequest request = new OperationRequest();
        request.setOperation("division");
        request.setFunction1_id(10L);
        request.setFunction2_id(11L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(418, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("teapot"));
    }

    @Test
    void performsBinaryAdditionOperation() {
        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(baseFunction));
        when(securityUtils.canAccessUserData(currentUser)).thenReturn(true);
        when(multipleSearchService.findPointsByFunction(10L)).thenReturn(basePoints);

        Functions function2 = new Functions();
        function2.setId(11L);
        function2.setUser(currentUser);
        function2.setName("g");
        function2.setType("array_tabulated");
        when(singleSearchService.findFunctionById(11L)).thenReturn(Optional.of(function2));
        when(securityUtils.canAccessUserData(function2.getUser())).thenReturn(true);

        List<FunctionPoints> function2Points = Arrays.asList(
                new FunctionPoints(function2, 0.0, 2.0),
                new FunctionPoints(function2, 1.0, 3.0),
                new FunctionPoints(function2, 2.0, 4.0)
        );
        when(multipleSearchService.findPointsByFunction(11L)).thenReturn(function2Points);

        Functions result = new Functions();
        result.setId(30L);
        result.setUser(currentUser);
        result.setName("result");
        result.setType("array_tabulated");
        when(singleSearchService.saveFunction(any(Functions.class))).thenReturn(result);

        List<FunctionPoints> resultPoints = Arrays.asList(
                new FunctionPoints(result, 0.0, 2.0),
                new FunctionPoints(result, 1.0, 4.0),
                new FunctionPoints(result, 2.0, 8.0)
        );
        when(multipleSearchService.findPointsByFunction(30L)).thenReturn(resultPoints);

        OperationRequest request = new OperationRequest();
        request.setOperation("add");
        request.setFunction1_id(10L);
        request.setFunction2_id(11L);

        ResponseEntity<?> response = controller.getOperationResult(request);

        assertEquals(200, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        List<PointResponse> body = (List<PointResponse>) response.getBody();
        assertNotNull(body);
        assertEquals(3, body.size());
        verify(singleSearchService, atLeastOnce()).saveFunctionPoint(any(FunctionPoints.class));
    }
}