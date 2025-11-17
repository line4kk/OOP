package repository;

import config.TestConfig;
import entity.CompositeFunctionElements;
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
class CompositeFunctionElementsRepositoryTest {

    @Autowired private CompositeFunctionElementsRepository compRepo;
    @Autowired private FunctionsRepository funcRepo;
    @Autowired private UsersRepository userRepo;

    @Test void testSave() {
        Users user = userRepo.save(new Users("user", "pass", "student", "array"));
        Functions composite = funcRepo.save(new Functions(user, "composite", "analytical", "composite"));
        Functions element = funcRepo.save(new Functions(user, "element", "linked_list_tabulated", "base"));
        CompositeFunctionElements compElem = new CompositeFunctionElements(composite, 1, element);
        assertNotNull(compRepo.save(compElem).getId());
    }

    @Test void testFindByComposite() {
        Users user = userRepo.save(new Users("user", "pass", "teacher", "linked_list"));
        Functions composite = funcRepo.save(new Functions(user, "composite", "analytical", "composite"));
        Functions elem1 = funcRepo.save(new Functions(user, "elem1", "array_tabulated", "base"));
        Functions elem2 = funcRepo.save(new Functions(user, "elem2", "linked_list_tabulated", "base"));
        compRepo.save(new CompositeFunctionElements(composite, 1, elem1));
        compRepo.save(new CompositeFunctionElements(composite, 2, elem2));
        assertEquals(2, compRepo.findByComposite(composite).size());
    }

    @Test void testFindByFunction() {
        Users user = userRepo.save(new Users("user", "pass", "student", "array"));
        Functions composite = funcRepo.save(new Functions(user, "composite", "analytical", "composite"));
        Functions element = funcRepo.save(new Functions(user, "element", "linked_list_tabulated", "base"));
        compRepo.save(new CompositeFunctionElements(composite, 1, element));
        assertEquals(1, compRepo.findByFunction(element).size());
    }

    @Test void testDelete() {
        Users user = userRepo.save(new Users("user", "pass", "student", "linked_list"));
        Functions composite = funcRepo.save(new Functions(user, "composite", "analytical", "composite"));
        Functions element = funcRepo.save(new Functions(user, "element", "array_tabulated", "base"));
        CompositeFunctionElements compElem = compRepo.save(new CompositeFunctionElements(composite, 1, element));
        compRepo.delete(compElem);
        assertFalse(compRepo.existsById(compElem.getId()));
    }

    @Test void testOrder() {
        Users user = userRepo.save(new Users("user", "pass", "student", "array"));
        Functions composite = funcRepo.save(new Functions(user, "composite", "analytical", "composite"));
        Functions element = funcRepo.save(new Functions(user, "element", "array_tabulated", "base"));
        compRepo.save(new CompositeFunctionElements(composite, 5, element));
        assertEquals(5, compRepo.findByComposite(composite).get(0).getFunctionOrder());
    }
}
