package com.example.bookstore.service;

import com.example.bookstore.domain.entity.DonHang;
import com.example.bookstore.domain.entity.YeuCauHuyDon;
import com.example.bookstore.domain.enums.CancelRequestStatus;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.DonHangRepository;
import com.example.bookstore.repository.YeuCauHuyDonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class CancelRequestService {

    private final DonHangRepository donHangRepository;
    private final YeuCauHuyDonRepository yeuCauHuyDonRepository;

    public CancelRequestService(DonHangRepository donHangRepository, YeuCauHuyDonRepository yeuCauHuyDonRepository) {
        this.donHangRepository = donHangRepository;
        this.yeuCauHuyDonRepository = yeuCauHuyDonRepository;
    }

    @Transactional
    public YeuCauHuyDon create(Long donHangId, String lyDo) {
        DonHang order = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        yeuCauHuyDonRepository.findByDonHangId(donHangId).ifPresent(x -> {
            throw new IllegalStateException("Đơn hàng đã có yêu cầu hủy");
        });

        if (order.getTrangThai() == OrderStatus.DANG_GIAO) {
            throw new IllegalStateException("Không thể hủy đơn hàng này");
        }

        if (order.getNgayDat() != null) {
            Duration age = Duration.between(order.getNgayDat(), Instant.now());
            if (age.toHours() > 24) {
                throw new IllegalStateException("Đã quá thời gian cho phép hủy đơn");
            }
        }

        YeuCauHuyDon req = new YeuCauHuyDon();
        req.setDonHang(order);
        req.setLyDoHuy(lyDo);
        req.setNgayYeuCau(Instant.now());
        req.setTrangThaiYeuCau(CancelRequestStatus.PENDING);
        return yeuCauHuyDonRepository.save(req);
    }

    @Transactional
    public String approve(Long requestId) {
        YeuCauHuyDon req = yeuCauHuyDonRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

        DonHang order = req.getDonHang();
        if (order.getTrangThai() == OrderStatus.DANG_GIAO) {
            throw new IllegalStateException("Không thể hủy đơn hàng này");
        }
        order.setTrangThai(OrderStatus.DA_HUY);
        req.setTrangThaiYeuCau(CancelRequestStatus.APPROVED);

        return "Hủy đơn hàng thành công";
    }

    @Transactional
    public String reject(Long requestId) {
        YeuCauHuyDon req = yeuCauHuyDonRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
        req.setTrangThaiYeuCau(CancelRequestStatus.REJECTED);
        return "Yêu cầu hủy đã bị từ chối";
    }
}

