package engine.repository;

import engine.Application;
import engine.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(classes = Application.class)
class UsersRepositoryTest {

    @Autowired private UsersRepository repo;

    @Test void testSave() {
        Users user = new Users("user1", "pass1", "student", "linked_list");
        assertNotNull(repo.save(user).getId());
    }

    @Test void testFindByUsername() {
        repo.save(new Users("alice", "pass", "teacher", "array"));
        assertEquals("teacher", repo.findByUsername("alice").getRole());
    }

    @Test void testDelete() {
        Users user = repo.save(new Users("bob", "pass", "admin", "linked_list"));
        repo.delete(user);
        assertFalse(repo.existsById(user.getId()));
    }

    @Test void testCount() {
        long count = repo.count();
        repo.save(new Users("test", "pass", "student", "array"));
        assertEquals(count + 1, repo.count());
    }

    @Test void testUpdate() {
        Users user = repo.save(new Users("user", "pass", "student", "array"));
        user.setFactoryType("linked_list");
        assertEquals("linked_list", repo.save(user).getFactoryType());
    }
}