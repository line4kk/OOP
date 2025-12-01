package engine.repository;

import engine.entity.FunctionPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunctionPointsRepository extends JpaRepository<FunctionPoints, Long> {

    List<FunctionPoints> findByFunctionId(Long functionId);

    List<FunctionPoints> findByFunction_Id(Long functionId);
}
