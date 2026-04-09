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
@Table(name = "nhan_vien")
public class NhanVien extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nhan_vien")
    private Long id;

    @Column(name = "ten_nhan_vien", nullable = false)
    private String tenNhanVien;

    @OneToMany(mappedBy = "nhanVienXuLy")
    private List<DonHang> donHangs = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }
}

