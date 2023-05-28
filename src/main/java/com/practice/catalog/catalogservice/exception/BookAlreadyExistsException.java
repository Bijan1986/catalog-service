package com.practice.catalog.catalogservice.exception;

public class BookAlreadyExistsException extends RuntimeException{
    public BookAlreadyExistsException(String isbn){
        super("Book with isbn: "+isbn+" already exists .");
    }
}
