package com.example.bookstore.service;

import com.example.bookstore.domain.entity.ChiTietDonHang;
import com.example.bookstore.domain.entity.DonHang;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.DonHangRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class ReportService {

    private final DonHangRepository donHangRepository;

    public ReportService(DonHangRepository donHangRepository) {
        this.donHangRepository = donHangRepository;
    }

    public SalesReportData buildSalesReport(Instant from, Instant to, String category, String status) {
        validateRange(from, to);

        List<DonHang> orders = donHangRepository.findByNgayDatBetween(from, to).stream()
                .filter(o -> {
                    if (status == null || status.isBlank()) {
                        return o.getTrangThai() == OrderStatus.DA_THANH_TOAN;
                    }
                    return o.getTrangThai().name().equalsIgnoreCase(status);
                })
                .filter(o -> {
                    if (category == null || category.isBlank()) {
                        return true;
                    }
                    return o.getChiTietDonHangs().stream()
                            .anyMatch(i -> category.equalsIgnoreCase(i.getSach().getDanhMuc()));
                })
                .toList();

        long totalOrders = orders.size();
        long totalRevenue = orders.stream().mapToLong(DonHang::getTongTien).sum();

        // Compare to previous period (same length)
        Duration period = Duration.between(from, to);
        Instant prevTo = from;
        Instant prevFrom = from.minus(period);
        long prevRevenue = donHangRepository.findByNgayDatBetween(prevFrom, prevTo).stream()
                .filter(o -> o.getTrangThai() == OrderStatus.DA_THANH_TOAN)
                .mapToLong(DonHang::getTongTien)
                .sum();
        Double growthPercent = prevRevenue == 0 ? null : ((totalRevenue - prevRevenue) * 100.0) / prevRevenue;

        Map<Long, BookAgg> bookAgg = orders.stream()
                .flatMap(o -> o.getChiTietDonHangs().stream())
                .filter(i -> category == null || category.isBlank() || category.equalsIgnoreCase(i.getSach().getDanhMuc()))
                .collect(Collectors.groupingBy(
                        i -> i.getSach().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            ChiTietDonHang first = list.get(0);
                            long qty = list.stream().mapToLong(ChiTietDonHang::getSoLuong).sum();
                            return new BookAgg(first.getSach().getId(), first.getSach().getTenSach(), first.getSach().getDanhMuc(), qty);
                        })
                ));

        long totalBooksSold = bookAgg.values().stream().mapToLong(BookAgg::soLuongBan).sum();
        List<BookAgg> topBooks = bookAgg.values().stream()
                .sorted(Comparator.comparingLong(BookAgg::soLuongBan).reversed())
                .limit(10)
                .toList();

        String message = totalOrders == 0 ? "Không có dữ liệu bán hàng phù hợp với tiêu chí tìm kiếm" : null;
        return new SalesReportData(from, to, totalOrders, totalRevenue, prevRevenue, growthPercent, totalBooksSold, topBooks, message);
    }

    public byte[] exportXlsx(SalesReportData data) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet s = wb.createSheet("SalesReport");
            int r = 0;

            Row header = s.createRow(r++);
            header.createCell(0).setCellValue("From");
            header.createCell(1).setCellValue(data.from().toString());
            Row header2 = s.createRow(r++);
            header2.createCell(0).setCellValue("To");
            header2.createCell(1).setCellValue(data.to().toString());

            r++;
            Row metrics = s.createRow(r++);
            metrics.createCell(0).setCellValue("TotalOrders");
            metrics.createCell(1).setCellValue(data.totalOrders());
            Row metrics2 = s.createRow(r++);
            metrics2.createCell(0).setCellValue("TotalRevenue");
            metrics2.createCell(1).setCellValue(data.totalRevenue());
            Row metrics2b = s.createRow(r++);
            metrics2b.createCell(0).setCellValue("PrevRevenue");
            metrics2b.createCell(1).setCellValue(data.prevRevenue());
            Row metrics2c = s.createRow(r++);
            metrics2c.createCell(0).setCellValue("GrowthPercent");
            metrics2c.createCell(1).setCellValue(data.growthPercent() == null ? "" : String.valueOf(data.growthPercent()));
            Row metrics3 = s.createRow(r++);
            metrics3.createCell(0).setCellValue("TotalBooksSold");
            metrics3.createCell(1).setCellValue(data.totalBooksSold());

            r++;
            Row tb = s.createRow(r++);
            tb.createCell(0).setCellValue("TopBooks");
            Row tbHeader = s.createRow(r++);
            tbHeader.createCell(0).setCellValue("BookId");
            tbHeader.createCell(1).setCellValue("BookName");
            tbHeader.createCell(2).setCellValue("Category");
            tbHeader.createCell(3).setCellValue("QtySold");

            for (BookAgg b : data.topBooks()) {
                Row row = s.createRow(r++);
                row.createCell(0).setCellValue(b.sachId());
                row.createCell(1).setCellValue(b.tenSach());
                row.createCell(2).setCellValue(b.danhMuc() == null ? "" : b.danhMuc());
                row.createCell(3).setCellValue(b.soLuongBan());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Trích xuất file thất bại. Vui lòng chia nhỏ khoảng thời gian hoặc thử lại sau");
        }
    }

    public byte[] exportPdf(SalesReportData data) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("Sales report"));
            doc.add(new Paragraph("From: " + data.from()));
            doc.add(new Paragraph("To: " + data.to()));
            doc.add(new Paragraph("Total orders: " + data.totalOrders()));
            doc.add(new Paragraph("Total revenue: " + data.totalRevenue()));
            doc.add(new Paragraph("Prev revenue: " + data.prevRevenue()));
            doc.add(new Paragraph("Growth percent: " + (data.growthPercent() == null ? "N/A" : data.growthPercent())));
            doc.add(new Paragraph("Total books sold: " + data.totalBooksSold()));
            if (data.message() != null) {
                doc.add(new Paragraph("Message: " + data.message()));
            }
            doc.add(new Paragraph("Top books:"));
            for (BookAgg b : data.topBooks()) {
                doc.add(new Paragraph("- " + b.tenSach() + " (" + b.sachId() + "): " + b.soLuongBan()));
            }
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Trích xuất file thất bại. Vui lòng chia nhỏ khoảng thời gian hoặc thử lại sau");
        }
    }

    private void validateRange(Instant from, Instant to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("Khoảng thời gian không hợp lệ");
        }
        Duration d = Duration.between(from, to);
        if (d.toDays() > 366) {
            throw new IllegalArgumentException("Khoảng thời gian truy xuất vượt quá giới hạn cho phép. Vui lòng chọn khoảng thời gian tối đa là 12 tháng");
        }
    }

    public record SalesReportData(
            Instant from,
            Instant to,
            long totalOrders,
            long totalRevenue,
            long prevRevenue,
            Double growthPercent,
            long totalBooksSold,
            List<BookAgg> topBooks
            ,
            String message
    ) {
    }

    public record BookAgg(Long sachId, String tenSach, String danhMuc, long soLuongBan) {
    }
}

