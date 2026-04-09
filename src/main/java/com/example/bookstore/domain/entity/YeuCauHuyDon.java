package com.example.bookstore.domain.entity;

import com.example.bookstore.domain.enums.CancelRequestStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "yeu_cau_huy_don")
public class YeuCauHuyDon extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_yeu_cau")
    private Long id;

    @OneToOne
    @JoinColumn(name = "ma_don_hang", nullable = false, unique = true)
    private DonHang donHang;

    @Column(name = "ly_do_huy", nullable = false, length = 1000)
    private String lyDoHuy;

    @Column(name = "ngay_yeu_cau", nullable = false)
    private Instant ngayYeuCau;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_yeu_cau", nullable = false)
    private CancelRequestStatus trangThaiYeuCau;

    public Long getId() {
        return id;
    }

    public DonHang getDonHang() {
        return donHang;
    }

    public void setDonHang(DonHang donHang) {
        this.donHang = donHang;
    }

    public String getLyDoHuy() {
        return lyDoHuy;
    }

    public void setLyDoHuy(String lyDoHuy) {
        this.lyDoHuy = lyDoHuy;
    }

    public Instant getNgayYeuCau() {
        return ngayYeuCau;
    }

    public void setNgayYeuCau(Instant ngayYeuCau) {
        this.ngayYeuCau = ngayYeuCau;
    }

    public CancelRequestStatus getTrangThaiYeuCau() {
        return trangThaiYeuCau;
    }

    public void setTrangThaiYeuCau(CancelRequestStatus trangThaiYeuCau) {
        this.trangThaiYeuCau = trangThaiYeuCau;
    }
}

