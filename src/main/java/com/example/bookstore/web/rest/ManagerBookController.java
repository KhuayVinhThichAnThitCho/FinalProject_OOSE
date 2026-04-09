package com.example.bookstore.web.rest;

import com.example.bookstore.service.PricingService;
import com.example.bookstore.web.dto.UpdateBookPriceRequest;
import com.example.bookstore.domain.entity.Book;
import com.example.bookstore.repository.BookRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/books")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerBookController {

    private final PricingService pricingService;
    private final BookRepository bookRepository;

    public ManagerBookController(PricingService pricingService, BookRepository bookRepository) {
        this.pricingService = pricingService;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public List<Book> list() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    public Book get(@PathVariable("id") Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }

    @PutMapping("/{id}/price")
    public PricingService.PricingResult updatePrice(
            @PathVariable("id") Long bookId,
            @Valid @RequestBody UpdateBookPriceRequest req
    ) {
        return pricingService.updatePrice(bookId, req.newSalePrice(), req.effectiveFrom(), req.allowLossSale());
    }
}

