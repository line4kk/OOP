package engine.service;

import engine.entity.CompositeFunctionElements;
import engine.entity.Functions;
import engine.entity.Users;
import engine.repository.CompositeFunctionElementsRepository;
import engine.repository.FunctionPointsRepository;
import engine.repository.FunctionsRepository;
import engine.repository.OperationsRepository;
import engine.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultipleSearchServiceTest {

    @Mock private UsersRepository usersRepository;
    @Mock private FunctionsRepository functionsRepository;
    @Mock private FunctionPointsRepository functionPointsRepository;
    @Mock private OperationsRepository operationsRepository;
    @Mock private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    @InjectMocks private MultipleSearchService service;

    private Users user;
    private Functions firstFunction;
    private Functions secondFunction;

    @BeforeEach
    void setUp() {
        user = new Users(1L, "demo", "hash", "user", "array", new ArrayList<>());
        firstFunction = new Functions();
        firstFunction.setId(1L);
        firstFunction.setUser(user);
        firstFunction.setName("alpha");
        firstFunction.setType("linked_list_tabulated");
        firstFunction.setSource("composite");
        secondFunction = new Functions();
        secondFunction.setId(2L);
        secondFunction.setUser(user);
        secondFunction.setName("beta");
        secondFunction.setType("array_tabulated");
    }

    @Test
    void findFunctionsByUserReturnsEmptyWhenUserNotFound() {
        when(usersRepository.findByUsername("missing")).thenReturn(null);

        List<Functions> result = service.findFunctionsByUser("missing");

        assertThat(result).isEmpty();
        verify(usersRepository).findByUsername("missing");
    }

    @Test
    void findFunctionsByUserReturnsFunctions() {
        when(usersRepository.findByUsername("demo")).thenReturn(user);
        when(functionsRepository.findByUser(user)).thenReturn(List.of(firstFunction, secondFunction));

        List<Functions> result = service.findFunctionsByUser("demo");

        assertThat(result).containsExactly(firstFunction, secondFunction);
        verify(functionsRepository).findByUser(user);
    }

    @Test
    void findCompositeFunctionElementsOrderedReturnsSortedElements() {
        CompositeFunctionElements first = new CompositeFunctionElements();
        first.setFunctionOrder(2);
        CompositeFunctionElements second = new CompositeFunctionElements();
        second.setFunctionOrder(1);
        when(functionsRepository.findById(5L)).thenReturn(Optional.of(firstFunction));
        when(compositeFunctionElementsRepository.findByComposite(firstFunction)).thenReturn(List.of(first, second));

        List<CompositeFunctionElements> result = service.findCompositeFunctionElementsOrdered(5L);

        assertThat(result).containsExactly(second, first);
    }

    @Test
    void findCompositeFunctionsFiltersBySource() {
        when(functionsRepository.findAll()).thenReturn(List.of(firstFunction, secondFunction));

        List<Functions> result = service.findCompositeFunctions();

        assertThat(result).containsExactly(firstFunction);
    }
}