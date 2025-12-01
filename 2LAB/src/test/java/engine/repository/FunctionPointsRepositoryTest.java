package engine.repository;

import engine.Application;
import engine.entity.FunctionPoints;
import engine.entity.Functions;
import engine.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(classes = Application.class)
class FunctionPointsRepositoryTest {

    @Autowired private FunctionPointsRepository pointsRepo;
    @Autowired private FunctionsRepository funcRepo;
    @Autowired private UsersRepository userRepo;

    @Test void testSave() {
        Users user = userRepo.save(new Users("user", "pass", "student", "linked_list"));
        Functions func = funcRepo.save(new Functions(user, "func", "array_tabulated", "base"));
        FunctionPoints point = new FunctionPoints(func, 1.0, 10.0);
        FunctionPoints saved = pointsRepo.save(point);
        assertNotNull(saved.getFunction());
        assertEquals(1.0, saved.getX_value());
    }

    @Test void testFindByFunction() {
        Users user = userRepo.save(new Users("user", "pass", "teacher", "array"));
        Functions func = funcRepo.save(new Functions(user, "func", "linked_list_tabulated", "base"));
        pointsRepo.save(new FunctionPoints(func, 1.0, 10.0));
        pointsRepo.save(new FunctionPoints(func, 2.0, 20.0));
        assertEquals(2, pointsRepo.findByFunctionId(func.getId()).size());
    }

    @Test void testDelete() {
        Users user = userRepo.save(new Users("user", "pass", "student", "linked_list"));
        Functions func = funcRepo.save(new Functions(user, "func", "analytical", "base"));
        FunctionPoints point = pointsRepo.save(new FunctionPoints(func, 3.0, 30.0));

        pointsRepo.delete(point);
        List<FunctionPoints> pointsAfterDelete = pointsRepo.findByFunctionId(func.getId());
        assertTrue(pointsAfterDelete.stream().noneMatch(p -> p.getX_value().equals(3.0)));
    }

    @Test void testMultiplePoints() {
        Users user = userRepo.save(new Users("user", "pass", "student", "array"));
        Functions func = funcRepo.save(new Functions(user, "func", "linked_list_tabulated", "composite"));
        pointsRepo.save(new FunctionPoints(func, 1.0, 10.0));
        pointsRepo.save(new FunctionPoints(func, 2.0, 20.0));
        pointsRepo.save(new FunctionPoints(func, 3.0, 30.0));
        assertEquals(3, pointsRepo.findByFunctionId(func.getId()).size());
    }

    @Test void testSameXDifferentFunctions() {
        Users user = userRepo.save(new Users("user", "pass", "student", "linked_list"));
        Functions func1 = funcRepo.save(new Functions(user, "func1", "analytical", "base"));
        Functions func2 = funcRepo.save(new Functions(user, "func2", "array_tabulated", "composite"));

        pointsRepo.save(new FunctionPoints(func1, 1.0, 10.0));
        pointsRepo.save(new FunctionPoints(func2, 1.0, 20.0));

        assertEquals(1, pointsRepo.findByFunctionId(func1.getId()).size());
        assertEquals(1, pointsRepo.findByFunctionId(func2.getId()).size());
    }
}