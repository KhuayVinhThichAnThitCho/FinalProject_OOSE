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
}

