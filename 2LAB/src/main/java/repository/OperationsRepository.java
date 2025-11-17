package repository;

import entity.Operations;
import entity.FunctionPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OperationsRepository extends JpaRepository<Operations, Long> {
    List<Operations> findByPoint1(FunctionPoints point1);
    List<Operations> findByPoint2(FunctionPoints point2);
}