package process.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import process.entities.BookElementEntity;

@Repository
public interface BookElementRepository extends JpaRepository<BookElementEntity, Long> {
    // Find Root BookElements
    List<BookElementEntity> findAllByParentIsNull();

    // Get Max Order among Root Entities
    BookElementEntity findTopByParentIsNullOrderByPositionDesc();
    // Get Max Order among non-Root Entities
    BookElementEntity findTopByParentOrderByPositionDesc(BookElementEntity parent);
}
