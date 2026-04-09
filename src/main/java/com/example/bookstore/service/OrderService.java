package com.example.bookstore.service;

import com.example.bookstore.domain.entity.*;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.domain.enums.PaymentStatus;
import com.example.bookstore.repository.*;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final DonHangRepository donHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final SachRepository sachRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final PaymentGateway paymentGateway;

    public OrderService(
            DonHangRepository donHangRepository,
            KhachHangRepository khachHangRepository,
            SachRepository sachRepository,
            PhuongThucThanhToanRepository phuongThucThanhToanRepository,
            ThanhToanRepository thanhToanRepository,
            PaymentGateway paymentGateway
    ) {
        this.donHangRepository = donHangRepository;
        this.khachHangRepository = khachHangRepository;
        this.sachRepository = sachRepository;
        this.phuongThucThanhToanRepository = phuongThucThanhToanRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public CheckoutResult checkout(
            Long khachHangId,
            List<ItemRequest> items,
            ShippingInfo shippingInfo,
            String paymentMethodCode,
            String username
    ) {
        KhachHang khachHang = khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));

        PhuongThucThanhToan method = phuongThucThanhToanRepository.findAll().stream()
                .filter(m -> m.getTenPhuongThuc().equalsIgnoreCase(paymentMethodCode) || paymentMethodCode.toUpperCase().startsWith(m.getTenPhuongThuc().toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));

        Map<Long, Integer> quantities = new HashMap<>();
        for (ItemRequest item : items) {
            quantities.merge(item.sachId(), item.soLuong(), Integer::sum);
        }

        List<Sach> books = sachRepository.findAllById(quantities.keySet());
        if (books.size() != quantities.size()) {
            throw new IllegalArgumentException("Có sách không tồn tại");
        }

        // Check inventory and compute total
        long total = 0L;
        for (Sach sach : books) {
            int qty = quantities.get(sach.getId());
            if (sach.getSoLuongTon() < qty) {
                throw new IllegalStateException("Không đủ hàng trong kho cho sách: " + sach.getTenSach());
            }
            total += sach.getGiaBan() * (long) qty;
        }

        DonHang order = new DonHang();
        order.setKhachHang(khachHang);
        order.setNgayDat(Instant.now());
        order.setTongTien(total);
        order.setTrangThai(OrderStatus.CHO_THANH_TOAN);

        // items
        List<ChiTietDonHang> orderItems = new ArrayList<>();
        for (Sach sach : books) {
            int qty = quantities.get(sach.getId());
            ChiTietDonHang line = new ChiTietDonHang();
            line.setDonHang(order);
            line.setSach(sach);
            line.setSoLuong(qty);
            line.setGia(sach.getGiaBan());
            orderItems.add(line);
        }
        order.getChiTietDonHangs().addAll(orderItems);

        // shipping
        ThongTinGiaoHang ttgh = new ThongTinGiaoHang();
        ttgh.setDonHang(order);
        ttgh.setNguoiNhan(shippingInfo.nguoiNhan());
        ttgh.setSoDienThoaiNhan(shippingInfo.soDienThoaiNhan());
        ttgh.setDiaChiGiaoHang(shippingInfo.diaChiGiaoHang());
        ttgh.setTrangThaiGiaoHang("CHO_XU_LY");
        order.setThongTinGiaoHang(ttgh);

        DonHang saved = donHangRepository.save(order);

        // Create payment PENDING
        ThanhToan payment = new ThanhToan();
        payment.setDonHang(saved);
        payment.setPhuongThucThanhToan(method);
        payment.setSoTien(total);
        payment.setTrangThai(PaymentStatus.PENDING);
        payment.setNgayThanhToan(Instant.now());
        thanhToanRepository.save(payment);

        // Charge via gateway (mock)
        PaymentResult result = paymentGateway.charge(new PaymentRequest(saved.getId(), total, paymentMethodCode, username));
        return handlePaymentResult(saved.getId(), payment.getId(), books, quantities, result);
    }

    @Transactional
    protected CheckoutResult handlePaymentResult(
            Long orderId,
            Long paymentId,
            List<Sach> books,
            Map<Long, Integer> quantities,
            PaymentResult result
    ) {
        DonHang order = donHangRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        ThanhToan payment = thanhToanRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán"));

        switch (result.status()) {
            case SUCCESS -> {
                order.setTrangThai(OrderStatus.DA_THANH_TOAN);
                payment.setTrangThai(PaymentStatus.SUCCESS);
                payment.setMaGiaoDichDoiTac(result.partnerTransactionId());

                // Decrease inventory
                try {
                    for (Sach sach : books) {
                        int qty = quantities.get(sach.getId());
                        sach.setSoLuongTon(sach.getSoLuongTon() - qty);
                    }
                    sachRepository.saveAll(books);
                } catch (OptimisticLockException ex) {
                    throw new IllegalStateException("Tồn kho bị thay đổi đồng thời, vui lòng thử lại");
                }

                donHangRepository.save(order);
                thanhToanRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getTrangThai(), "Đặt hàng và Thanh toán thành công");
            }
            case INSUFFICIENT_FUNDS -> {
                order.setTrangThai(OrderStatus.CHO_THANH_TOAN);
                payment.setTrangThai(PaymentStatus.FAILED);
                donHangRepository.save(order);
                thanhToanRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getTrangThai(), "Số dư tài khoản không đủ để thực hiện thanh toán");
            }
            case MAINTENANCE -> {
                order.setTrangThai(OrderStatus.CHO_THANH_TOAN);
                payment.setTrangThai(PaymentStatus.FAILED);
                donHangRepository.save(order);
                thanhToanRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getTrangThai(), "Hệ thống thanh toán đang bảo trì, vui lòng thử lại sau");
            }
            case USER_CANCELLED -> {
                order.setTrangThai(OrderStatus.CHO_THANH_TOAN);
                payment.setTrangThai(PaymentStatus.CANCELLED);
                donHangRepository.save(order);
                thanhToanRepository.save(payment);
                return new CheckoutResult(order.getId(), order.getTrangThai(), "Bạn đã hủy đặt hàng");
            }
        }
        throw new IllegalStateException("Trạng thái thanh toán không hỗ trợ");
    }

    public record ItemRequest(Long sachId, Integer soLuong) {}

    public record ShippingInfo(String nguoiNhan, String soDienThoaiNhan, String diaChiGiaoHang) {}

    public record CheckoutResult(Long orderId, OrderStatus orderStatus, String message) {}
}

