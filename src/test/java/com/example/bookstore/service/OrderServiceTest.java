package com.example.bookstore.service;

import com.example.bookstore.domain.entity.KhachHang;
import com.example.bookstore.domain.entity.PhuongThucThanhToan;
import com.example.bookstore.domain.entity.Sach;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.DonHangRepository;
import com.example.bookstore.repository.KhachHangRepository;
import com.example.bookstore.repository.PhuongThucThanhToanRepository;
import com.example.bookstore.repository.SachRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({OrderService.class, MockPaymentGateway.class})
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    KhachHangRepository khachHangRepository;
    @Autowired
    SachRepository sachRepository;
    @Autowired
    PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    @Autowired
    DonHangRepository donHangRepository;

    Long khId;
    Long sachId;

    @BeforeEach
    void seed() {
        KhachHang kh = new KhachHang();
        kh.setTenKhachHang("T");
        kh.setEmail("t@test.com");
        kh.setSoDienThoai("090");
        kh.setDiaChi("HCM");
        khId = khachHangRepository.save(kh).getId();

        Sach s = new Sach();
        s.setTenSach("B");
        s.setGiaBan(100L);
        s.setGiaNhap(80L);
        s.setSoLuongTon(10);
        sachId = sachRepository.save(s).getId();

        PhuongThucThanhToan pt = new PhuongThucThanhToan();
        pt.setTenPhuongThuc("ONLINE");
        phuongThucThanhToanRepository.save(pt);
    }

    @Test
    void checkout_success_setsPaid_and_decreaseInventory() {
        OrderService.CheckoutResult res = orderService.checkout(
                khId,
                List.of(new OrderService.ItemRequest(sachId, 2)),
                new OrderService.ShippingInfo("A", "090", "D"),
                "ONLINE_OK",
                "customer"
        );
        assertThat(res.orderStatus()).isEqualTo(OrderStatus.DA_THANH_TOAN);
        assertThat(sachRepository.findById(sachId).orElseThrow().getSoLuongTon()).isEqualTo(8);
        assertThat(donHangRepository.findById(res.orderId()).orElseThrow().getTrangThai()).isEqualTo(OrderStatus.DA_THANH_TOAN);
    }
}

