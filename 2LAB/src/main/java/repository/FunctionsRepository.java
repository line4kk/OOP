package repository;

import entity.Functions;
import entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FunctionsRepository extends JpaRepository<Functions, Long> {
    List<Functions> findByUser(Users user);
    List<Functions> findByName(String name);
    List<Functions> findByType(String type);
}