package process.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import process.entities.BookElementEntity;
import process.repo.BookElementRepository;

@Service
public class BookElementService {
    @Autowired
    private BookElementRepository bookElementRepository;

    public BookElementEntity getById(long id) {
        Optional<BookElementEntity> entity = bookElementRepository.findById(id);
        return entity.get();
    }

    public List<BookElementEntity> getRootElements() {
        List<BookElementEntity> list = bookElementRepository.findAllByParentIsNull();
        return list;
    }

    public BookElementEntity save(BookElementEntity entity) {
        calculatePositionForNewEntity(entity);

        BookElementEntity savedEntity = bookElementRepository.saveAndFlush(entity);

        return savedEntity;
    }

    private void calculatePositionForNewEntity(BookElementEntity entity) {
        // Calculating Position for New Entities only
        // For Existing Entities please use Up/Down shifting
        if (entity.getId() != null) {
            return;
        }

        BookElementEntity parent = entity.getParent();
        BookElementEntity entityWithMaxPosition;
        if (parent == null) {
            entityWithMaxPosition = bookElementRepository.findTopByParentIsNullOrderByPositionDesc();
        } else {
            entityWithMaxPosition = bookElementRepository.findTopByParentOrderByPositionDesc(parent);
        }
        Long position = entityWithMaxPosition == null ? 10 : entityWithMaxPosition.getPosition();
        position = position == null ? 10 : position + 10;

        entity.setPosition(position);
    }

    public void delete(BookElementEntity bookElementEntity) {
        Long id = bookElementEntity.getId();
        bookElementRepository.deleteById(id);
    }
}
