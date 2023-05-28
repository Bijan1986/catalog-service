package com.practice.catalog.catalogservice.repository;

import com.practice.catalog.catalogservice.domain.Book;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookRepository implements BookRepository{
    private static final Map<String, Book> books = new ConcurrentHashMap<>();

    @Override
    public Iterable<Book> findAllBooks() {
        return books.values();
    }

    @Override
    public Optional<Book> findBookByIsbn(String isbn) {
        return existsByIsbn(isbn)?Optional.of(books.get(isbn)):Optional.empty();
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return books.containsKey(isbn);
    }

    @Override
    public Book save(Book book) {
        books.put(book.isbn().toString(),book);
        return book;
    }

    @Override
    public void deleteByIsbn(String isbn) {
        books.remove(isbn);
    }
}
