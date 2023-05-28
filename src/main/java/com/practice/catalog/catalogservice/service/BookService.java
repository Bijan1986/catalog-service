package com.practice.catalog.catalogservice.service;

import com.practice.catalog.catalogservice.domain.Book;
import com.practice.catalog.catalogservice.exception.BookAlreadyExistsException;
import com.practice.catalog.catalogservice.exception.BookNotFoundException;
import com.practice.catalog.catalogservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    // view list of books
    public Iterable<Book> findAllBook() {
        return bookRepository.findAllBooks();
    }

    // view book details
    public Book viewBookDetails(String isbn) {
        return bookRepository.findBookByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    // add book to catolog
    public Book addBookToCatalog(Book book) {
        if (bookRepository.existsByIsbn(book.isbn().toString())) {
            throw new BookAlreadyExistsException(book.isbn().toString());
        }
        return bookRepository.save(book);
    }

    // remove book from the catalog
    public void removeBookFromCatalog(String isbn) {
        bookRepository.deleteByIsbn(isbn);
    }

    // edit book details
    public Book editBook(Book book) {
        return bookRepository.existsByIsbn(book.isbn().toString())
                ?
                bookRepository.save(book) : addBookToCatalog(book);

    }
}
