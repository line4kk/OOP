package repository;

import config.TestConfig;
import entity.Functions;
import entity.Users;
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
class FunctionsRepositoryTest {

    @Autowired private FunctionsRepository funcRepo;
    @Autowired private UsersRepository userRepo;

    @Test void testSave() {
        Users user = userRepo.save(new Users("user", "pass", "student", "array"));
        Functions func = new Functions(user, "func1", "analytical", "base");
        assertNotNull(funcRepo.save(func).getId());
    }

    @Test void testFindByUser() {
        Users user = userRepo.save(new Users("user", "pass", "teacher", "linked_list"));
        funcRepo.save(new Functions(user, "func1", "array_tabulated", "composite"));
        funcRepo.save(new Functions(user, "func2", "analytical", "base"));
        assertEquals(2, funcRepo.findByUser(user).size());
    }

    @Test void testFindByName() {
        Users user = userRepo.save(new Users("user", "pass", "student", "array"));
        funcRepo.save(new Functions(user, "unique_func", "linked_list_tabulated", "base"));
        assertEquals(1, funcRepo.findByName("unique_func").size());
    }

    @Test void testFindByType() {
        Users user = userRepo.save(new Users("user", "pass", "student", "linked_list"));
        funcRepo.save(new Functions(user, "func", "analytical", "composite"));
        assertTrue(funcRepo.findByType("analytical").size() >= 1);
    }

    @Test void testDelete() {
        Users user = userRepo.save(new Users("user", "pass", "student", "array"));
        Functions func = funcRepo.save(new Functions(user, "func", "array_tabulated", "composite"));
        funcRepo.delete(func);
        assertFalse(funcRepo.existsById(func.getId()));
    }
}
