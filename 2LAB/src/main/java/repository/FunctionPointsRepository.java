package repository;

import entity.FunctionPoints;
import entity.Functions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FunctionPointsRepository extends JpaRepository<FunctionPoints, Long> {
    List<FunctionPoints> findByFunction(Functions function);
}
