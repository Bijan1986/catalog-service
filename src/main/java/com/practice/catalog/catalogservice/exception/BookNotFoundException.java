package com.practice.catalog.catalogservice.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String isbn){
        super("Book with isbn "+isbn+" was not found .");
    }
}
