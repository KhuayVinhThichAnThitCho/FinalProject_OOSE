package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    default java.util.List<Book> findAllBooks() {
        return findAll();
    }

    default java.util.Optional<Book> findBookById(Long bookId) {
        return findById(bookId);
    }
}

