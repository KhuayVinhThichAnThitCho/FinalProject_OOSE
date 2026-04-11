package com.example.bookstore.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "books")
public class Book extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "sale_price", nullable = false)
    private Long salePrice;

    @Column(name = "sale_price_effective_from")
    private Instant salePriceEffectiveFrom;

    @Column(name = "cost_price", nullable = false)
    private Long costPrice;

    @Column(name = "category")
    private String category;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPrice() {
        return salePrice;
    }

    public void setPrice(Long salePrice) {
        this.salePrice = salePrice;
    }

    public void setSalePrice(Long newPrice, Instant applyTime) {
        setPrice(newPrice);
        setSalePriceEffectiveFrom(applyTime == null ? Instant.now() : applyTime);
    }

    public Instant getSalePriceEffectiveFrom() {
        return salePriceEffectiveFrom;
    }

    public void setSalePriceEffectiveFrom(Instant salePriceEffectiveFrom) {
        this.salePriceEffectiveFrom = salePriceEffectiveFrom;
    }

    public Long getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Long costPrice) {
        this.costPrice = costPrice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public record PriceValidation(boolean canApply, String message) {}

    public PriceValidation validatePriceUpdate(Long newPrice, boolean allowLossSale) {
        if (newPrice == null || newPrice <= 0) {
            throw new IllegalArgumentException("Giá bán không hợp lệ");
        }
        if (newPrice < costPrice && !allowLossSale) {
            return new PriceValidation(false,
                    "Giá bán hiện tại đang thấp hơn giá vốn. Bạn có chắc chắn muốn tiếp tục?");
        }
        return new PriceValidation(true, null);
    }

    public void applyNewPrice(Long newPrice, Instant effectiveFrom) {
        this.salePrice = newPrice;
        this.salePriceEffectiveFrom = effectiveFrom == null ? Instant.now() : effectiveFrom;
    }

    public void checkStock(int quantity) {
        if (stockQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock for book: " + title);
        }
    }

    public void deductStock(int quantity) {
        checkStock(quantity);
        this.stockQuantity -= quantity;
    }
}

