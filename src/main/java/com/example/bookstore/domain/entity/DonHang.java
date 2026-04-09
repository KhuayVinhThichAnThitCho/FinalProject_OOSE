package com.example.bookstore.domain.entity;

import com.example.bookstore.domain.enums.OrderStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "don_hang")
public class DonHang extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_don_hang")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ma_khach_hang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "ma_nhan_vien_xu_ly")
    private NhanVien nhanVienXuLy;

    @Column(name = "ngay_dat", nullable = false)
    private Instant ngayDat;

    @Column(name = "tong_tien", nullable = false)
    private Long tongTien;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private OrderStatus trangThai;

    @Version
    private Long version;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietDonHang> chiTietDonHangs = new ArrayList<>();

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThanhToan> thanhToans = new ArrayList<>();

    @OneToOne(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private ThongTinGiaoHang thongTinGiaoHang;

    @OneToOne(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private YeuCauHuyDon yeuCauHuyDon;

    public Long getId() {
        return id;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien getNhanVienXuLy() {
        return nhanVienXuLy;
    }

    public void setNhanVienXuLy(NhanVien nhanVienXuLy) {
        this.nhanVienXuLy = nhanVienXuLy;
    }

    public Instant getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(Instant ngayDat) {
        this.ngayDat = ngayDat;
    }

    public Long getTongTien() {
        return tongTien;
    }

    public void setTongTien(Long tongTien) {
        this.tongTien = tongTien;
    }

    public OrderStatus getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(OrderStatus trangThai) {
        this.trangThai = trangThai;
    }

    public List<ChiTietDonHang> getChiTietDonHangs() {
        return chiTietDonHangs;
    }

    public List<ThanhToan> getThanhToans() {
        return thanhToans;
    }

    public ThongTinGiaoHang getThongTinGiaoHang() {
        return thongTinGiaoHang;
    }

    public void setThongTinGiaoHang(ThongTinGiaoHang thongTinGiaoHang) {
        this.thongTinGiaoHang = thongTinGiaoHang;
    }

    public YeuCauHuyDon getYeuCauHuyDon() {
        return yeuCauHuyDon;
    }

    public void setYeuCauHuyDon(YeuCauHuyDon yeuCauHuyDon) {
        this.yeuCauHuyDon = yeuCauHuyDon;
    }
}

