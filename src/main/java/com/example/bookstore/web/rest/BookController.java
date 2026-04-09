package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER')")
    public List<Book> list() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER')")
    public Book get(@PathVariable("id") Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }
}

