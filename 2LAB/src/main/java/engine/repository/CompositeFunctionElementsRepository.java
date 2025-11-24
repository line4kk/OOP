package engine.repository;

import engine.entity.CompositeFunctionElements;
import engine.entity.Functions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompositeFunctionElementsRepository extends JpaRepository<CompositeFunctionElements, Long> {
    List<CompositeFunctionElements> findByComposite(Functions composite);
    List<CompositeFunctionElements> findByFunction(Functions function);
}