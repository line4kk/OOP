package engine.service;

import engine.entity.CompositeFunctionElements;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleSearchServiceTest {

    @Mock private UsersRepository usersRepository;
    @Mock private FunctionsRepository functionsRepository;
    @Mock private FunctionPointsRepository functionPointsRepository;
    @Mock private CompositeFunctionElementsRepository compositeFunctionElementsRepository;

    @InjectMocks private SingleSearchService service;

    private Users user;
    private Functions function;
    private FunctionPoints point;

    @BeforeEach
    void setUp() {
        user = new Users(1L, "demo", "hash", "user", "array", List.of());
        function = new Functions();
        function.setId(10L);
        function.setUser(user);
        point = new FunctionPoints();
        point.setId(100L);
        point.setFunction(function);
    }

    @Test
    void findUserByUsernameReturnsEntity() {
        when(usersRepository.findByUsername("demo")).thenReturn(user);

        Users result = service.findUserByUsername("demo");

        assertThat(result).isEqualTo(user);
        verify(usersRepository).findByUsername("demo");
    }

    @Test
    void findFunctionByNameAndUserDelegatesToRepository() {
        when(functionsRepository.findByNameAndUser("f", user)).thenReturn(Optional.of(function));

        Optional<Functions> result = service.findFunctionByNameAndUser("f", user);

        assertThat(result).contains(function);
        verify(functionsRepository).findByNameAndUser("f", user);
    }

    @Test
    void saveUserPersistsEntity() {
        when(usersRepository.save(any(Users.class))).thenReturn(user);

        Users saved = service.saveUser(user);

        assertThat(saved).isEqualTo(user);
        verify(usersRepository).save(user);
    }

    @Test
    void findFunctionPointByXUsesRepository() {
        when(functionPointsRepository.findByFunctionIdAndX(10L, 3.5)).thenReturn(Optional.of(point));

        Optional<FunctionPoints> result = service.findFunctionPointByX(10L, 3.5);

        assertThat(result).contains(point);
        verify(functionPointsRepository).findByFunctionIdAndX(10L, 3.5);
    }

    @Test
    void isFunctionUsedInCompositionChecksRepository() {
        when(compositeFunctionElementsRepository.findByFunction(function)).thenReturn(List.of(new CompositeFunctionElements()));

        boolean used = service.isFunctionUsedInComposition(function);

        assertThat(used).isTrue();
        verify(compositeFunctionElementsRepository).findByFunction(eq(function));
    }
}