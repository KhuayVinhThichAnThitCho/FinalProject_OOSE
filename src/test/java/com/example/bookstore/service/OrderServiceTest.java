package com.example.bookstore.service;

import com.example.bookstore.domain.entity.Book;
import com.example.bookstore.domain.entity.Customer;
import com.example.bookstore.domain.entity.PaymentMethod;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.PaymentMethodRepository;
import com.example.bookstore.repository.PaymentRepository;
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
    CustomerRepository customerRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    PaymentMethodRepository paymentMethodRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    PaymentRepository paymentRepository;

    Long customerId;
    Long bookId;

    @BeforeEach
    void seed() {
        Customer kh = new Customer();
        kh.setFullName("T");
        kh.setEmail("t@test.com");
        kh.setPhone("090");
        kh.setAddress("HCM");
        customerId = customerRepository.save(kh).getId();

        Book b = new Book();
        b.setTitle("B");
        b.setSalePrice(100L);
        b.setCostPrice(80L);
        b.setStockQuantity(10);
        bookId = bookRepository.save(b).getId();

        PaymentMethod pm = new PaymentMethod();
        pm.setCode("ONLINE");
        paymentMethodRepository.save(pm);
    }

    @Test
    void checkout_success_setsPaid_and_decreaseInventory() {
        OrderService.CheckoutResult res = orderService.checkout(
                customerId,
                List.of(new OrderService.ItemRequest(bookId, 2)),
                new OrderService.ShippingInfo("A", "090", "D"),
                0L,
                "ONLINE_OK",
                "customer"
        );
        assertThat(res.orderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(bookRepository.findById(bookId).orElseThrow().getStockQuantity()).isEqualTo(8);
        assertThat(orderRepository.findById(res.orderId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
    }
}

