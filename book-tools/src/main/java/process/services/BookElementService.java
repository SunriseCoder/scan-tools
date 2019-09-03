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
        // Calculating Position for New Entities only
        // For Existing Entities please use Up/Down shifting
        if (entity.getId() == null) {
            Long position = calculatePositionForNewEntity(entity);
            entity.setPosition(position);
        }

        BookElementEntity savedEntity = bookElementRepository.saveAndFlush(entity);

        return savedEntity;
    }

    public void save(List<BookElementEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        Long position = calculatePositionForNewEntity(entities.get(0));
        for (BookElementEntity entity : entities) {
            if (entity.getId() == null) {
                entity.setPosition(position);
                position += 10;
            }
        }

        bookElementRepository.saveAll(entities);
    }

    private Long calculatePositionForNewEntity(BookElementEntity entity) {
        BookElementEntity parent = entity.getParent();
        BookElementEntity entityWithMaxPosition;
        if (parent == null) {
            entityWithMaxPosition = bookElementRepository.findTopByParentIsNullOrderByPositionDesc();
        } else {
            entityWithMaxPosition = bookElementRepository.findTopByParentOrderByPositionDesc(parent);
        }
        Long position = entityWithMaxPosition == null ? 10 : entityWithMaxPosition.getPosition();
        position = position == null ? 10 : position + 10;

        return position;
    }

    public void delete(BookElementEntity bookElementEntity) {
        Long id = bookElementEntity.getId();
        bookElementRepository.deleteById(id);
    }
}
