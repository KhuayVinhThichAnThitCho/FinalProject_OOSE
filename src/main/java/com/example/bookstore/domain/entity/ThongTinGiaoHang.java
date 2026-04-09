package com.example.bookstore.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "thong_tin_giao_hang")
public class ThongTinGiaoHang extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "ma_don_hang", nullable = false, unique = true)
    private DonHang donHang;

    @Column(name = "dia_chi_giao_hang", nullable = false)
    private String diaChiGiaoHang;

    @Column(name = "nguoi_nhan", nullable = false)
    private String nguoiNhan;

    @Column(name = "so_dien_thoai_nhan", nullable = false)
    private String soDienThoaiNhan;

    @Column(name = "trang_thai_giao_hang", nullable = false)
    private String trangThaiGiaoHang;

    public Long getId() {
        return id;
    }

    public DonHang getDonHang() {
        return donHang;
    }

    public void setDonHang(DonHang donHang) {
        this.donHang = donHang;
    }

    public String getDiaChiGiaoHang() {
        return diaChiGiaoHang;
    }

    public void setDiaChiGiaoHang(String diaChiGiaoHang) {
        this.diaChiGiaoHang = diaChiGiaoHang;
    }

    public String getNguoiNhan() {
        return nguoiNhan;
    }

    public void setNguoiNhan(String nguoiNhan) {
        this.nguoiNhan = nguoiNhan;
    }

    public String getSoDienThoaiNhan() {
        return soDienThoaiNhan;
    }

    public void setSoDienThoaiNhan(String soDienThoaiNhan) {
        this.soDienThoaiNhan = soDienThoaiNhan;
    }

    public String getTrangThaiGiaoHang() {
        return trangThaiGiaoHang;
    }

    public void setTrangThaiGiaoHang(String trangThaiGiaoHang) {
        this.trangThaiGiaoHang = trangThaiGiaoHang;
    }
}

