package process.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import process.entities.BookElementEntity;
import process.repo.BookRepository;

@Service
public class BookService {
    @Autowired
    private BookRepository repository;

    public BookElementEntity getById(long id) {
        Optional<BookElementEntity> entity = repository.findById(id);
        return entity.get();
    }
}
