package engine.repository;

import engine.entity.Functions;
import engine.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FunctionsRepository extends JpaRepository<Functions, Long> {

    List<Functions> findByUser(Users user);

    List<Functions> findByName(String name);

    List<Functions> findByType(String type);

    List<Functions> findBySource(String source);

    Optional<Functions> findByNameAndUser(String name, Users user);

    boolean existsByNameAndUser(String name, Users user);

    @Query("SELECT f FROM Functions f WHERE f.user = :user AND f.type = :type")
    List<Functions> findByUserAndType(@Param("user") Users user, @Param("type") String type);

    @Query("SELECT f FROM Functions f WHERE f.user = :user AND f.name = :name")
    List<Functions> findByUserAndName(@Param("user") Users user, @Param("name") String name);
}