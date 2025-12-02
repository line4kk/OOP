package engine.controller;

import engine.dto.CompositeCreateRequest;
import engine.dto.FunctionSamplingRequest;
import engine.entity.CompositeFunctionElements;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import engine.repository.CompositeFunctionElementsRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticalFunctionControllerTest {

    @Mock
    private MultipleSearchService multipleSearchService;
    @Mock
    private SingleSearchService singleSearchService;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private CompositeFunctionElementsRepository compositeRepository;

    @InjectMocks
    private AnalyticalFunctionController controller;

    private Users user;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setId(1L);
        user.setUsername("owner");
    }

    @Test
    void createCompositeFunctionValidatesInput() {
        CompositeCreateRequest request = new CompositeCreateRequest();
        request.setFunctionIdsInOrder(List.of(1L));
        request.setName(" ");

        when(securityUtils.getCurrentUser()).thenReturn(null);
        ResponseEntity<?> forbidden = controller.createCompositeFunction(request);
        assertEquals(403, forbidden.getStatusCodeValue());

        when(securityUtils.getCurrentUser()).thenReturn(user);
        ResponseEntity<?> tooFew = controller.createCompositeFunction(request);
        assertEquals(400, tooFew.getStatusCodeValue());

        request.setFunctionIdsInOrder(List.of(1L, 2L));
        ResponseEntity<?> emptyName = controller.createCompositeFunction(request);
        assertEquals(400, emptyName.getStatusCodeValue());
    }

    @Test
    void createCompositeFunctionHandlesConflictsAndSaves() {
        CompositeCreateRequest request = new CompositeCreateRequest();
        request.setName("comp");
        request.setFunctionIdsInOrder(List.of(5L, 6L));

        Functions component1 = new Functions(user, "f1", "analytical", "base");
        component1.setId(5L);
        Functions component2 = new Functions(user, "f2", "analytical", "base");
        component2.setId(6L);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(multipleSearchService.findFunctionsByName("comp")).thenReturn(List.of());
        when(singleSearchService.findFunctionById(5L)).thenReturn(Optional.of(component1));
        when(singleSearchService.findFunctionById(6L)).thenReturn(Optional.of(component2));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        when(singleSearchService.saveFunction(any())).thenAnswer(invocation -> {
            Functions saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ResponseEntity<?> response = controller.createCompositeFunction(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(compositeRepository, times(2)).save(any(CompositeFunctionElements.class));
    }

    @Test
    void createCompositeFunctionRejectsDuplicatesAndMissingComponents() {
        CompositeCreateRequest request = new CompositeCreateRequest();
        request.setName("comp");
        request.setFunctionIdsInOrder(List.of(1L, 1L));

        when(securityUtils.getCurrentUser()).thenReturn(user);

        ResponseEntity<?> duplicateId = controller.createCompositeFunction(request);
        assertEquals(400, duplicateId.getStatusCodeValue());

        request.setFunctionIdsInOrder(List.of(2L, 3L));
        when(singleSearchService.findFunctionById(2L)).thenReturn(Optional.empty());
        ResponseEntity<?> notFound = controller.createCompositeFunction(request);
        assertEquals(404, notFound.getStatusCodeValue());
    }

    @Test
    void getCompositeFunctionElementsValidatesTypeAndAccess() {
        when(securityUtils.getCurrentUser()).thenReturn(null);
        ResponseEntity<?> forbidden = controller.getCompositeFunctionElements(1L);
        assertEquals(403, forbidden.getStatusCodeValue());

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(1L)).thenReturn(Optional.empty());
        ResponseEntity<?> notFound = controller.getCompositeFunctionElements(1L);
        assertEquals(404, notFound.getStatusCodeValue());

        Functions wrong = new Functions(user, "f", "array_tabulated", "base");
        when(singleSearchService.findFunctionById(2L)).thenReturn(Optional.of(wrong));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        ResponseEntity<?> badType = controller.getCompositeFunctionElements(2L);
        assertEquals(400, badType.getStatusCodeValue());
    }

    @Test
    void getCompositeFunctionElementsReturnsOrderedList() {
        Functions composite = new Functions(user, "c", "analytical", "composite");
        composite.setId(10L);
        CompositeFunctionElements element = new CompositeFunctionElements(composite, 0, new Functions());
        element.setId(7L);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(composite));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        when(multipleSearchService.findCompositeFunctionElementsOrdered(10L)).thenReturn(List.of(element));

        ResponseEntity<?> response = controller.getCompositeFunctionElements(10L);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("7"));
    }

    @Test
    void samplingValidatesRequestsEarly() {
        FunctionSamplingRequest request = new FunctionSamplingRequest();
        request.setFunction_id(1L);
        request.setAnalytical_function_id(2L);
        request.setX_from(0.0);
        request.setX_to(-1.0);
        request.setCount(1);

        when(securityUtils.getCurrentUser()).thenReturn(null);
        assertEquals(403, controller.addFunctionPointsBySampling(1L, request).getStatusCodeValue());

        when(securityUtils.getCurrentUser()).thenReturn(user);
        assertEquals(400, controller.addFunctionPointsBySampling(2L, request).getStatusCodeValue());

        request.setX_to(1.0);
        request.setCount(1);
        assertEquals(400, controller.addFunctionPointsBySampling(1L, request).getStatusCodeValue());
    }

    @Test
    void samplingHandlesMissingFunctionsAndTypes() {
        FunctionSamplingRequest request = new FunctionSamplingRequest();
        request.setFunction_id(1L);
        request.setAnalytical_function_id(2L);
        request.setX_from(0.0);
        request.setX_to(2.0);
        request.setCount(3);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(1L)).thenReturn(Optional.empty());
        assertEquals(404, controller.addFunctionPointsBySampling(1L, request).getStatusCodeValue());

        Functions target = new Functions(user, "t", "analytical", "base");
        when(singleSearchService.findFunctionById(1L)).thenReturn(Optional.of(target));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        assertEquals(400, controller.addFunctionPointsBySampling(1L, request).getStatusCodeValue());

        target.setType("array_tabulated");
        when(singleSearchService.findFunctionById(2L)).thenReturn(Optional.empty());
        assertEquals(404, controller.addFunctionPointsBySampling(1L, request).getStatusCodeValue());
    }

    @Test
    void samplingPersistsGeneratedPoints() {
        FunctionSamplingRequest request = new FunctionSamplingRequest();
        request.setFunction_id(1L);
        request.setAnalytical_function_id(2L);
        request.setX_from(0.0);
        request.setX_to(2.0);
        request.setCount(3);

        Functions target = new Functions(user, "t", "array_tabulated", "base");
        target.setId(1L);
        Functions analytic = new Functions(user, "identity", "analytical", "base");
        analytic.setId(2L);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        when(singleSearchService.findFunctionById(1L)).thenReturn(Optional.of(target));
        when(singleSearchService.findFunctionById(2L)).thenReturn(Optional.of(analytic));
        when(singleSearchService.findFunctionPointByX(anyLong(), anyDouble())).thenReturn(Optional.empty());
        when(singleSearchService.saveFunctionPoint(any())).thenAnswer(invocation -> {
            FunctionPoints point = invocation.getArgument(0);
            point.setId((long) (point.getX_value() * 10));
            return point;
        });

        ResponseEntity<?> response = controller.addFunctionPointsBySampling(1L, request);

        assertEquals(200, response.getStatusCodeValue());
        verify(singleSearchService, times(3)).saveFunctionPoint(any(FunctionPoints.class));
    }
}
