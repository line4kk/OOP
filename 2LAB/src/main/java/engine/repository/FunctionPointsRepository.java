package engine.repository;

import engine.entity.FunctionPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunctionPointsRepository extends JpaRepository<FunctionPoints, Long> {

    List<FunctionPoints> findByFunction_Id(Long functionId);

    default List<FunctionPoints> findByFunctionId(Long functionId) {
        return findByFunction_Id(functionId);
    }

    java.util.Optional<FunctionPoints> findByIdAndFunction_Id(Long pointId, Long functionId);

    @org.springframework.data.jpa.repository.Query("select p from FunctionPoints p where p.function.id = :functionId and p.x_value = :xValue")
    java.util.Optional<FunctionPoints> findByFunctionIdAndX(Long functionId, Double xValue);
}
