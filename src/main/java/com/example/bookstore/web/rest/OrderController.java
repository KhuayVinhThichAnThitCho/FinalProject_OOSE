package com.example.bookstore.web.rest;

import com.example.bookstore.domain.entity.DonHang;
import com.example.bookstore.repository.DonHangRepository;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.web.dto.CheckoutRequest;
import com.example.bookstore.web.dto.CheckoutResponse;
import com.example.bookstore.web.dto.OrderDetailResponse;
import com.example.bookstore.web.dto.OrderItemDto;
import com.example.bookstore.web.dto.OrderSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final DonHangRepository donHangRepository;

    public OrderController(OrderService orderService, DonHangRepository donHangRepository) {
        this.orderService = orderService;
        this.donHangRepository = donHangRepository;
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request, Authentication authentication) {
        String username = authentication == null ? "anonymous" : authentication.getName();

        List<OrderService.ItemRequest> items = request.items().stream()
                .map(i -> new OrderService.ItemRequest(i.sachId(), i.soLuong()))
                .toList();

        OrderService.ShippingInfo shippingInfo = new OrderService.ShippingInfo(
                request.nguoiNhan(),
                request.soDienThoaiNhan(),
                request.diaChiGiaoHang()
        );

        OrderService.CheckoutResult result = orderService.checkout(
                request.khachHangId(),
                items,
                shippingInfo,
                request.paymentMethodCode(),
                username
        );
        return new CheckoutResponse(result.orderId(), result.orderStatus(), result.message());
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<OrderSummaryResponse> listByCustomer(@RequestParam("khachHangId") Long khachHangId) {
        return donHangRepository.findByKhachHangIdOrderByNgayDatDesc(khachHangId).stream()
                .map(o -> new OrderSummaryResponse(o.getId(), o.getNgayDat(), o.getTongTien(), o.getTrangThai()))
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderDetailResponse getDetail(
            @PathVariable("id") Long orderId,
            @RequestParam("khachHangId") Long khachHangId
    ) {
        DonHang o = donHangRepository.findByIdAndKhachHangId(orderId, khachHangId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin đơn hàng."));

        List<OrderItemDto> items = o.getChiTietDonHangs().stream()
                .map(i -> new OrderItemDto(
                        i.getSach().getId(),
                        i.getSach().getTenSach(),
                        i.getSoLuong(),
                        i.getGia()
                ))
                .toList();

        OrderDetailResponse.ShippingInfo shipping = o.getThongTinGiaoHang() == null ? null :
                new OrderDetailResponse.ShippingInfo(
                        o.getThongTinGiaoHang().getDiaChiGiaoHang(),
                        o.getThongTinGiaoHang().getNguoiNhan(),
                        o.getThongTinGiaoHang().getSoDienThoaiNhan(),
                        o.getThongTinGiaoHang().getTrangThaiGiaoHang()
                );

        return new OrderDetailResponse(
                o.getId(),
                o.getNgayDat(),
                o.getTongTien(),
                o.getTrangThai(),
                shipping,
                items
        );
    }
}

