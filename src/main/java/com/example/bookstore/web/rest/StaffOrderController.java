package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.DonHang;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.DonHangRepository;
import com.example.bookstore.web.dto.OrderSummaryResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/orders")
public class StaffOrderController {

    private final DonHangRepository donHangRepository;

    public StaffOrderController(DonHangRepository donHangRepository) {
        this.donHangRepository = donHangRepository;
    }

    @GetMapping("/pending")
    public List<OrderSummaryResponse> listPending() {
        return donHangRepository.findByTrangThai(OrderStatus.CHO_XU_LY).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getNgayDat(), o.getTongTien(), o.getTrangThai()))
                .toList();
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable("id") Long orderId) {
        DonHang o = donHangRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (o.getTrangThai() != OrderStatus.CHO_XU_LY) {
            throw new IllegalStateException("Đơn hàng đã được xác nhận");
        }
        o.setTrangThai(OrderStatus.DANG_GIAO);
        donHangRepository.save(o);
        return "Đơn hàng đã chuyển sang trạng thái đang giao hàng!";
    }

    @PostMapping("/{id}/cancel-processing")
    public String cancelProcessing(@PathVariable("id") Long orderId) {
        DonHang o = donHangRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        return "Đã hủy xử lý đơn hàng!";
    }
}

