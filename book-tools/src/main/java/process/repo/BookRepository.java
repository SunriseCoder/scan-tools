package process.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import process.entities.BookElementEntity;

@Repository
public interface BookRepository extends JpaRepository<BookElementEntity, Long> {
    List<BookElementEntity> findAllByParentIsNull();
}
