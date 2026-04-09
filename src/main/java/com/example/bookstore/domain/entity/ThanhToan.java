package com.example.bookstore.domain.entity;

import com.example.bookstore.domain.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "thanh_toan")
public class ThanhToan extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_thanh_toan")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ma_don_hang")
    private DonHang donHang;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ma_phuong_thuc")
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "so_tien", nullable = false)
    private Long soTien;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, columnDefinition = "varchar(50)")
    private PaymentStatus trangThai;

    @Column(name = "ngay_thanh_toan", nullable = false)
    private Instant ngayThanhToan;

    @Column(name = "ma_giao_dich_doi_tac")
    private String maGiaoDichDoiTac;

    public Long getId() {
        return id;
    }

    public DonHang getDonHang() {
        return donHang;
    }

    public void setDonHang(DonHang donHang) {
        this.donHang = donHang;
    }

    public PhuongThucThanhToan getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(PhuongThucThanhToan phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public Long getSoTien() {
        return soTien;
    }

    public void setSoTien(Long soTien) {
        this.soTien = soTien;
    }

    public PaymentStatus getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(PaymentStatus trangThai) {
        this.trangThai = trangThai;
    }

    public Instant getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(Instant ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public String getMaGiaoDichDoiTac() {
        return maGiaoDichDoiTac;
    }

    public void setMaGiaoDichDoiTac(String maGiaoDichDoiTac) {
        this.maGiaoDichDoiTac = maGiaoDichDoiTac;
    }
}

