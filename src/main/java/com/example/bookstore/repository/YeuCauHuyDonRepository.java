package com.example.bookstore.repository;

import com.example.bookstore.domain.entity.YeuCauHuyDon;
import com.example.bookstore.domain.enums.CancelRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface YeuCauHuyDonRepository extends JpaRepository<YeuCauHuyDon, Long> {

    Optional<YeuCauHuyDon> findByDonHangId(Long donHangId);

    List<YeuCauHuyDon> findByTrangThaiYeuCau(CancelRequestStatus status);
}

