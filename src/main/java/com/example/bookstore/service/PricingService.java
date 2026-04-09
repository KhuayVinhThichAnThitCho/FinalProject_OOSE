package com.example.bookstore.service;

import com.example.bookstore.domain.entity.Sach;
import com.example.bookstore.repository.SachRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PricingService {

    private final SachRepository sachRepository;

    public PricingService(SachRepository sachRepository) {
        this.sachRepository = sachRepository;
    }

    @Transactional
    public PricingResult updatePrice(Long sachId, Long giaBanMoi, Instant thoiGianApDung, boolean chapNhanBanLo) {
        Sach sach = sachRepository.findById(sachId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách"));

        if (giaBanMoi <= 0) {
            throw new IllegalArgumentException("Giá bán không hợp lệ");
        }

        if (giaBanMoi < sach.getGiaNhap() && !chapNhanBanLo) {
            return new PricingResult(false,
                    "Giá bán hiện tại đang thấp hơn giá vốn. Bạn có chắc chắn muốn tiếp tục?",
                    sach.getGiaNhap(),
                    sach.getGiaBan(),
                    giaBanMoi);
        }

        sach.setGiaBan(giaBanMoi);
        sach.setGiaBanApDungTu(thoiGianApDung == null ? Instant.now() : thoiGianApDung);
        sachRepository.save(sach);
        return new PricingResult(true, "Cập nhật giá bán thành công", sach.getGiaNhap(), sach.getGiaBan(), giaBanMoi);
    }

    public record PricingResult(
            boolean updated,
            String message,
            Long giaNhap,
            Long giaBanCu,
            Long giaBanMoi
    ) {
    }
}

