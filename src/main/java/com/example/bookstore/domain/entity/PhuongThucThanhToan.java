package com.example.bookstore.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phuong_thuc_thanh_toan")
public class PhuongThucThanhToan extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phuong_thuc")
    private Long id;

    @Column(name = "ten_phuong_thuc", nullable = false, unique = true)
    private String tenPhuongThuc;

    @Column(name = "mo_ta")
    private String moTa;

    @OneToMany(mappedBy = "phuongThucThanhToan")
    private List<ThanhToan> thanhToans = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    public void setTenPhuongThuc(String tenPhuongThuc) {
        this.tenPhuongThuc = tenPhuongThuc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}

