package repository;

import config.TestConfig;
import entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class OperationsRepositoryTest {

    @Autowired private OperationsRepository opRepo;
    @Autowired private FunctionPointsRepository pointsRepo;
    @Autowired private FunctionsRepository funcRepo;
    @Autowired private UsersRepository userRepo;

    @Test void testSave() {
        Users user = userRepo.save(new Users("user", "pass", "student", "linked_list"));
        Functions func = funcRepo.save(new Functions(user, "func", "array_tabulated", "base"));
        FunctionPoints point = pointsRepo.save(new FunctionPoints(func, 1.0, 10.0));
        Operations op = new Operations("add", point, 15.0);
        assertNotNull(opRepo.save(op).getId());
    }

    @Test void testSaveTwoPoints() {
        Users user = userRepo.save(new Users("user", "pass", "teacher", "array"));
        Functions func = funcRepo.save(new Functions(user, "func", "linked_list_tabulated", "base"));
        FunctionPoints p1 = pointsRepo.save(new FunctionPoints(func, 1.0, 10.0));
        FunctionPoints p2 = pointsRepo.save(new FunctionPoints(func, 2.0, 20.0));
        Operations op = new Operations("multiply", p1, p2, 200.0);
        assertNotNull(opRepo.save(op).getId());
    }

    @Test void testFindByPoint1() {
        Users user = userRepo.save(new Users("user", "pass", "student", "linked_list"));
        Functions func = funcRepo.save(new Functions(user, "func", "analytical", "base"));
        FunctionPoints p1 = pointsRepo.save(new FunctionPoints(func, 1.0, 10.0));
        FunctionPoints p2 = pointsRepo.save(new FunctionPoints(func, 2.0, 20.0));
        opRepo.save(new Operations("add", p1, p2, 30.0));
        assertEquals(1, opRepo.findByPoint1(p1).size());
    }

    @Test void testFindByPoint2() {
        Users user = userRepo.save(new Users("user", "pass", "student", "array"));
        Functions func = funcRepo.save(new Functions(user, "func", "linked_list_tabulated", "base"));
        FunctionPoints p1 = pointsRepo.save(new FunctionPoints(func, 1.0, 10.0));
        FunctionPoints p2 = pointsRepo.save(new FunctionPoints(func, 2.0, 20.0));
        opRepo.save(new Operations("subtract", p1, p2, 10.0));
        assertEquals(1, opRepo.findByPoint2(p2).size());
    }

    @Test void testDelete() {
        Users user = userRepo.save(new Users("user", "pass", "student", "linked_list"));
        Functions func = funcRepo.save(new Functions(user, "func", "array_tabulated", "base"));
        FunctionPoints point = pointsRepo.save(new FunctionPoints(func, 1.0, 10.0));
        Operations op = opRepo.save(new Operations("divide", point, 5.0));
        opRepo.delete(op);
        assertFalse(opRepo.existsById(op.getId()));
    }
}
