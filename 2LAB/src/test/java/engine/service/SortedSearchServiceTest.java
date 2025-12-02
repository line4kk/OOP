package engine.service;

import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import engine.repository.CompositeFunctionElementsRepository;
import engine.repository.FunctionPointsRepository;
import engine.repository.FunctionsRepository;
import engine.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SortedSearchServiceTest {

    @Mock private UsersRepository usersRepository;
    @Mock private FunctionsRepository functionsRepository;
    @Mock private FunctionPointsRepository functionPointsRepository;
    @Mock private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    @InjectMocks private SortedSearchService service;

    private Users firstUser;
    private Users secondUser;

    @BeforeEach
    void setUp() {
        firstUser = new Users(1L, "anna", "hash", "user", "array", List.of());
        secondUser = new Users(2L, "boris", "hash", "user", "array", List.of());
    }

    @Test
    void findUsersSortedByUsernameOrdersAlphabetically() {
        when(usersRepository.findAll()).thenReturn(List.of(secondUser, firstUser));

        List<Users> result = service.findUsersSortedByUsername();

        assertThat(result).containsExactly(firstUser, secondUser);
        verify(usersRepository).findAll();
    }

    @Test
    void findFunctionsSortedByNameOrdersAlphabetically() {
        Functions second = new Functions();
        second.setName("zeta");
        Functions first = new Functions();
        first.setName("alpha");
        when(functionsRepository.findAll()).thenReturn(List.of(second, first));

        List<Functions> result = service.findFunctionsSortedByName();

        assertThat(result).extracting(Functions::getName).containsExactly("alpha", "zeta");
    }

    @Test
    void findPointsSortedByXReturnsEmptyWhenFunctionNotExists() {
        when(functionsRepository.existsById(anyLong())).thenReturn(false);

        List<FunctionPoints> result = service.findPointsSortedByX(5L);

        assertThat(result).isEmpty();
    }

    @Test
    void findPointsSortedByYSortsByValue() {
        when(functionsRepository.existsById(10L)).thenReturn(true);
        FunctionPoints first = new FunctionPoints();
        first.setY_value(9.0);
        FunctionPoints second = new FunctionPoints();
        second.setY_value(1.0);
        when(functionPointsRepository.findByFunction_Id(10L)).thenReturn(List.of(first, second));

        List<FunctionPoints> result = service.findPointsSortedByY(10L);

        assertThat(result).extracting(FunctionPoints::getY_value).containsExactly(1.0, 9.0);
        verify(functionPointsRepository).findByFunction_Id(10L);
    }
}