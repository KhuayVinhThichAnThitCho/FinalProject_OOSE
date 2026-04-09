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
    public PricingResult updatePrice(Long bookId, Long newSalePrice, Instant effectiveFrom, boolean allowLossSale) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        if (newSalePrice <= 0) {
            throw new IllegalArgumentException("Invalid sale price");
        }

        if (newSalePrice < book.getCostPrice() && !allowLossSale) {
            return new PricingResult(false,
                    "Giá bán hiện tại đang thấp hơn giá vốn. Bạn có chắc chắn muốn tiếp tục?",
                    book.getCostPrice(),
                    book.getSalePrice(),
                    newSalePrice);
        }

        book.setSalePrice(newSalePrice);
        book.setSalePriceEffectiveFrom(effectiveFrom == null ? Instant.now() : effectiveFrom);
        bookRepository.save(book);
        return new PricingResult(true, "Cập nhật giá bán thành công", book.getCostPrice(), book.getSalePrice(), newSalePrice);
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

