package com.practice.catalog.catalogservice.repository;

import com.practice.catalog.catalogservice.domain.Book;
import org.hibernate.validator.constraints.ISBN;

import java.util.Optional;

public interface BookRepository {
    Iterable<Book> findAllBooks();
    Optional<Book> findBookByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
    Book save(Book book);
    void deleteByIsbn(String isbn);


}
