package com.practice.catalog.catalogservice.demo;

import com.practice.catalog.catalogservice.domain.Book;
import com.practice.catalog.catalogservice.repository.BookRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("testdata")
@AllArgsConstructor
public class BookDataLoader {
    private final BookRepository bookRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void loadBookTestData(){
        var book1 = new Book("132131434","Northern Lights","Lyra Silverstar",9.90);
        var book2 = new Book("1234124412","Polar Journey","Lyra Polarson",9.90);
        bookRepository.save(book1);
        bookRepository.save(book2);
    }
}
