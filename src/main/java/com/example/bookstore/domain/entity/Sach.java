package com.example.bookstore.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "sach")
public class Sach extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_sach")
    private Long id;

    @Column(name = "ten_sach", nullable = false)
    private String tenSach;

    @Column(name = "gia_ban", nullable = false)
    private Long giaBan;

    @Column(name = "gia_ban_ap_dung_tu")
    private Instant giaBanApDungTu;

    @Column(name = "gia_nhap", nullable = false)
    private Long giaNhap;

    @Column(name = "danh_muc")
    private String danhMuc;

    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public String getTenSach() {
        return tenSach;
    }

    public void setTenSach(String tenSach) {
        this.tenSach = tenSach;
    }

    public Long getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(Long giaBan) {
        this.giaBan = giaBan;
    }

    public Instant getGiaBanApDungTu() {
        return giaBanApDungTu;
    }

    public void setGiaBanApDungTu(Instant giaBanApDungTu) {
        this.giaBanApDungTu = giaBanApDungTu;
    }

    public Long getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(Long giaNhap) {
        this.giaNhap = giaNhap;
    }

    public String getDanhMuc() {
        return danhMuc;
    }

    public void setDanhMuc(String danhMuc) {
        this.danhMuc = danhMuc;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(Integer soLuongTon) {
        this.soLuongTon = soLuongTon;
    }
}

