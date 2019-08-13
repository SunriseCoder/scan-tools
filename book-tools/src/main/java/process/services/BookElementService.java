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
        BookElementEntity savedEntity = bookElementRepository.saveAndFlush(entity);
        return savedEntity;
    }

    public void delete(BookElementEntity bookElementEntity) {
        Long id = bookElementEntity.getId();
        bookElementRepository.deleteById(id);
    }
}
