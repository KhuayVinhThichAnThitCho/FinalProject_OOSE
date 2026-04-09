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
@Table(name = "khach_hang")
public class KhachHang extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_khach_hang")
    private Long id;

    @Column(name = "ten_khach_hang", nullable = false)
    private String tenKhachHang;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "so_dien_thoai", nullable = false)
    private String soDienThoai;

    @Column(name = "dia_chi", nullable = false)
    private String diaChi;

    @OneToMany(mappedBy = "khachHang")
    private List<DonHang> donHangs = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}

