package com.example.bookstore.service;

import com.example.bookstore.domain.entity.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PricingService {

    private final BookRepository bookRepository;

    public PricingService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public PricingResult confirmUpdate(Long bookId, Long newSalePrice, Instant applyTime, boolean allowLossSale) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách"));

        Book.PriceValidation v = book.validatePriceUpdate(newSalePrice, allowLossSale);
        if (!v.canApply()) {
            return new PricingResult(false, v.message(), book.getCostPrice(), book.getPrice(), newSalePrice);
        }

        book.applyNewPrice(newSalePrice, applyTime);
        bookRepository.save(book);
        return new PricingResult(true, "Cập nhật giá bán thành công", book.getCostPrice(), book.getPrice(), newSalePrice);
    }

    // Backward-compatible name
    @Transactional
    public PricingResult updatePrice(Long bookId, Long newSalePrice, Instant effectiveFrom, boolean allowLossSale) {
        return confirmUpdate(bookId, newSalePrice, effectiveFrom, allowLossSale);
    }

    public record PricingResult(
            boolean updated,
            String message,
            Long costPrice,
            Long oldSalePrice,
            Long newSalePrice
    ) {
    }
}

