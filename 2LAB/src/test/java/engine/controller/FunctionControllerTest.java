package engine.controller;

import engine.dto.FunctionCreateRequest;
import engine.dto.PointRequest;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FunctionControllerTest {

    @Mock
    private SingleSearchService singleSearchService;
    @Mock
    private MultipleSearchService multipleSearchService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private FunctionController controller;

    private Users user;
    private Functions function;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setId(1L);
        user.setUsername("test");

        function = new Functions();
        function.setId(10L);
        function.setName("f1");
        function.setType("array_tabulated");
        function.setSource("base");
        function.setUser(user);
    }

    @Test
    void getAllFunctionsReturnsForbiddenWhenUserMissing() {
        when(securityUtils.getCurrentUser()).thenReturn(null);

        ResponseEntity<?> response = controller.getAllUserFunctions();

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Forbidden", response.getBody());
    }

    @Test
    void getAllFunctionsReturnsMappedResponses() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(multipleSearchService.findFunctionsByUser("test")).thenReturn(List.of(function));

        ResponseEntity<?> response = controller.getAllUserFunctions();

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("f1"));
    }

    @Test
    void getAnalyticFunctionsFiltersByType() {
        Functions analytic = new Functions();
        analytic.setId(2L);
        analytic.setName("analyt");
        analytic.setType("analytical");
        analytic.setSource("base");
        analytic.setUser(user);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(multipleSearchService.findFunctionsByUser("test")).thenReturn(List.of(function, analytic));

        ResponseEntity<?> response = controller.getAllUserAnalyticFunctions();

        assertEquals(200, response.getStatusCodeValue());
        String body = response.getBody().toString();
        assertTrue(body.contains("analyt"));
        assertFalse(body.contains("f1"));
    }

    @Test
    void createFunctionValidatesRequestAndDuplicates() {
        FunctionCreateRequest request = new FunctionCreateRequest();
        request.setName(" ");
        request.setType("array_tabulated");
        request.setSource("base");

        when(securityUtils.getCurrentUser()).thenReturn(user);

        ResponseEntity<?> invalidName = controller.createFunction(request);
        assertEquals(400, invalidName.getStatusCodeValue());

        request.setName("f1");
        request.setType("invalid");
        ResponseEntity<?> invalidType = controller.createFunction(request);
        assertEquals(400, invalidType.getStatusCodeValue());

        request.setType("array_tabulated");
        when(multipleSearchService.findFunctionsByName("f1")).thenReturn(List.of(function));
        ResponseEntity<?> duplicate = controller.createFunction(request);
        assertEquals(409, duplicate.getStatusCodeValue());
    }

    @Test
    void createFunctionSavesWhenValid() {
        FunctionCreateRequest request = new FunctionCreateRequest();
        request.setName("new");
        request.setType("array_tabulated");
        request.setSource("base");

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(multipleSearchService.findFunctionsByName("new")).thenReturn(List.of());
        when(singleSearchService.saveFunction(any())).thenAnswer(invocation -> {
            Functions saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        ResponseEntity<?> response = controller.createFunction(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("new"));
        verify(singleSearchService).saveFunction(any());
    }

    @Test
    void getFunctionByIdHandlesMissingOrForbidden() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.empty());

        ResponseEntity<?> notFound = controller.getFunctionById(10L);
        assertEquals(404, notFound.getStatusCodeValue());

        Users otherUser = new Users();
        otherUser.setId(2L);
        function.setUser(otherUser);
        when(singleSearchService.findFunctionById(11L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(otherUser)).thenReturn(false);

        ResponseEntity<?> forbidden = controller.getFunctionById(11L);
        assertEquals(403, forbidden.getStatusCodeValue());
    }

    @Test
    void getFunctionByIdReturnsResponseWhenAccessible() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);

        ResponseEntity<?> response = controller.getFunctionById(10L);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("f1"));
    }

    @Test
    void updateFunctionValidatesRequestAndDuplicates() {
        FunctionCreateRequest request = new FunctionCreateRequest();
        request.setName("new");
        request.setType("bad");
        request.setSource("base");

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);

        ResponseEntity<?> invalid = controller.updateFunction(10L, request);
        assertEquals(400, invalid.getStatusCodeValue());

        request.setType("array_tabulated");
        Functions conflict = new Functions(user, "new", "array_tabulated", "base");
        conflict.setId(99L);
        when(multipleSearchService.findFunctionsByName("new")).thenReturn(List.of(conflict));

        ResponseEntity<?> duplicate = controller.updateFunction(10L, request);
        assertEquals(409, duplicate.getStatusCodeValue());
    }

    @Test
    void updateFunctionSavesChanges() {
        FunctionCreateRequest request = new FunctionCreateRequest();
        request.setName("upd");
        request.setType("linked_list_tabulated");
        request.setSource("operation");

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        when(multipleSearchService.findFunctionsByName("upd")).thenReturn(List.of());
        when(singleSearchService.saveFunction(function)).thenReturn(function);

        ResponseEntity<?> response = controller.updateFunction(10L, request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("upd", function.getName());
        verify(singleSearchService).saveFunction(function);
    }

    @Test
    void deleteFunctionValidatesUsageAndAccess() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        when(singleSearchService.isFunctionUsedInComposition(function)).thenReturn(true);

        ResponseEntity<?> used = controller.deleteFunction(10L);
        assertEquals(405, used.getStatusCodeValue());

        when(singleSearchService.isFunctionUsedInComposition(function)).thenReturn(false);
        ResponseEntity<?> ok = controller.deleteFunction(10L);
        assertEquals(200, ok.getStatusCodeValue());
        verify(singleSearchService).deleteFunction(10L);
    }

    @Test
    void getFunctionPointsOrdersById() {
        FunctionPoints p1 = new FunctionPoints(function, 1.0, 2.0);
        p1.setId(2L);
        FunctionPoints p0 = new FunctionPoints(function, 0.0, 1.0);
        p0.setId(1L);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        when(multipleSearchService.findPointsByFunction(10L)).thenReturn(Arrays.asList(p1, p0));

        ResponseEntity<?> response = controller.getFunctionPoints(10L);

        assertEquals(200, response.getStatusCodeValue());
        String body = response.getBody().toString();
        assertTrue(body.indexOf("1.0") < body.indexOf("2.0"));
    }

    @Test
    void addFunctionPointsValidatesInputAndConflicts() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);

        ResponseEntity<?> empty = controller.addFunctionPoints(10L, new ArrayList<>());
        assertEquals(400, empty.getStatusCodeValue());

        PointRequest invalid = new PointRequest();
        ResponseEntity<?> missingValues = controller.addFunctionPoints(10L, List.of(invalid));
        assertEquals(400, missingValues.getStatusCodeValue());

        PointRequest p1 = new PointRequest();
        p1.setX(1.0);
        p1.setY(2.0);
        PointRequest p2 = new PointRequest();
        p2.setX(1.0);
        p2.setY(3.0);

        ResponseEntity<?> duplicateNew = controller.addFunctionPoints(10L, List.of(p1, p2));
        assertEquals(409, duplicateNew.getStatusCodeValue());

        when(singleSearchService.findFunctionPointByX(10L, 1.0)).thenReturn(Optional.of(new FunctionPoints()));
        ResponseEntity<?> duplicateExisting = controller.addFunctionPoints(10L, List.of(p1));
        assertEquals(409, duplicateExisting.getStatusCodeValue());
    }

    @Test
    void addFunctionPointsPersistsAndReturnsResponses() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        when(singleSearchService.findFunctionPointByX(10L, 1.0)).thenReturn(Optional.empty());

        when(singleSearchService.saveFunctionPoint(any())).thenAnswer(invocation -> {
            FunctionPoints point = invocation.getArgument(0);
            point.setId(5L);
            return point;
        });

        PointRequest request = new PointRequest();
        request.setX(1.0);
        request.setY(2.0);

        ResponseEntity<?> response = controller.addFunctionPoints(10L, List.of(request));

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("1.0"));
        verify(singleSearchService).saveFunctionPoint(any());
    }

    @Test
    void updateFunctionPointValidatesConflictsAndSaves() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);

        PointRequest invalid = new PointRequest();
        ResponseEntity<?> missing = controller.updateFunctionPoint(10L, 1L, invalid);
        assertEquals(400, missing.getStatusCodeValue());

        FunctionPoints existing = new FunctionPoints(function, 0.0, 0.0);
        existing.setId(1L);
        when(singleSearchService.findFunctionPointByFunction(1L, 10L)).thenReturn(Optional.of(existing));
        PointRequest update = new PointRequest();
        update.setX(2.0);
        update.setY(4.0);

        FunctionPoints other = new FunctionPoints(function, 2.0, 5.0);
        other.setId(2L);
        when(singleSearchService.findFunctionPointByX(10L, 2.0)).thenReturn(Optional.of(other));
        ResponseEntity<?> duplicate = controller.updateFunctionPoint(10L, 1L, update);
        assertEquals(409, duplicate.getStatusCodeValue());

        when(singleSearchService.findFunctionPointByX(10L, 2.0)).thenReturn(Optional.empty());
        when(singleSearchService.saveFunctionPoint(existing)).thenReturn(existing);

        ResponseEntity<?> response = controller.updateFunctionPoint(10L, 1L, update);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2.0, existing.getX_value());
    }

    @Test
    void deleteFunctionPointHandlesMissingAndDeletes() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);
        when(singleSearchService.findFunctionPointByFunction(1L, 10L)).thenReturn(Optional.empty());

        ResponseEntity<?> missing = controller.deleteFunctionPoint(10L, 1L);
        assertEquals(404, missing.getStatusCodeValue());

        FunctionPoints existing = new FunctionPoints(function, 1.0, 2.0);
        when(singleSearchService.findFunctionPointByFunction(1L, 10L)).thenReturn(Optional.of(existing));

        ResponseEntity<?> response = controller.deleteFunctionPoint(10L, 1L);
        assertEquals(200, response.getStatusCodeValue());
        verify(singleSearchService).deleteFunctionPoint(existing);
    }

    @Test
    void deleteAllFunctionPointsIteratesThroughFoundPoints() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(singleSearchService.findFunctionById(10L)).thenReturn(Optional.of(function));
        when(securityUtils.canAccessUserData(user)).thenReturn(true);

        FunctionPoints p1 = new FunctionPoints(function, 0.0, 0.0);
        FunctionPoints p2 = new FunctionPoints(function, 1.0, 1.0);
        when(multipleSearchService.findPointsByFunction(10L)).thenReturn(List.of(p1, p2));

        ResponseEntity<?> response = controller.deleteAllFunctionPoints(10L);

        assertEquals(200, response.getStatusCodeValue());
        verify(singleSearchService, times(2)).deleteFunctionPoint(any(FunctionPoints.class));
    }
}