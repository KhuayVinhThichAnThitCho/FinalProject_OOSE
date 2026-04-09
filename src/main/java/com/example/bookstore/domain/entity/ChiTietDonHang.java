package com.example.bookstore.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chi_tiet_don_hang")
public class ChiTietDonHang extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_chi_tiet")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ma_don_hang")
    private DonHang donHang;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ma_sach")
    private Sach sach;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "gia", nullable = false)
    private Long gia;

    public Long getId() {
        return id;
    }

    public DonHang getDonHang() {
        return donHang;
    }

    public void setDonHang(DonHang donHang) {
        this.donHang = donHang;
    }

    public Sach getSach() {
        return sach;
    }

    public void setSach(Sach sach) {
        this.sach = sach;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Long getGia() {
        return gia;
    }

    public void setGia(Long gia) {
        this.gia = gia;
    }
}

