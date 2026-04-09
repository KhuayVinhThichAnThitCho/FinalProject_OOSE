package com.example.bookstore.web.dto;

import com.example.bookstore.domain.enums.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        Instant ngayDat,
        Long tongTien,
        OrderStatus trangThai,
        ShippingInfo shipping,
        List<OrderItemDto> items
) {
    public record ShippingInfo(
            String diaChiGiaoHang,
            String nguoiNhan,
            String soDienThoaiNhan,
            String trangThaiGiaoHang
    ) {
    }
}

