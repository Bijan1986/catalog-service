package com.practice.catalog.catalogservice.controller;

import com.practice.catalog.catalogservice.domain.Book;
import com.practice.catalog.catalogservice.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public Iterable<Book> getAllBooks(){
        return bookService.findAllBook();
    }

    @GetMapping("{isbn}")
    public Book getBookByIsbn(@PathVariable String isbn){
        return bookService.viewBookDetails(isbn);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book saveBook(@Valid @RequestBody Book book){
        return bookService.addBookToCatalog(book);
    }

    @DeleteMapping("{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable String isbn){
        bookService.removeBookFromCatalog(isbn);
    }

    @PutMapping
    public Book editBook(@Valid @RequestBody Book book){
        return bookService.editBook(book);
    }

}
