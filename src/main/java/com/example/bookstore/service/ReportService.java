package com.example.bookstore.service;

import com.example.bookstore.domain.entity.Book;
import com.example.bookstore.domain.entity.Order;
import com.example.bookstore.domain.entity.OrderDetail;
import com.example.bookstore.domain.enums.OrderStatus;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class ReportService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public ReportService(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    public ReportOptions showReportOptions() {
        List<String> categories = bookRepository.findAllBooks().stream()
                .map(Book::getCategory)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .toList();
        List<String> orderStatuses = Arrays.stream(OrderStatus.values()).map(Enum::name).toList();
        return new ReportOptions(orderStatuses, categories, OrderStatus.DELIVERED.name());
    }

    public SalesReportData generateReport(Instant fromDate, Instant toDate, String category, String orderStatus) {
        return buildSalesReport(fromDate, toDate, category, orderStatus);
    }

    public SalesReportData buildSalesReport(Instant from, Instant to, String category, String status) {
        validateRange(from, to);

        List<Order> orders = getSalesData(from, to, category, status);

        long totalOrders = orders.size();
        long totalRevenue = orders.stream().mapToLong(Order::getTotalAmount).sum();

        Duration period = Duration.between(from, to);
        Instant prevTo = from;
        Instant prevFrom = from.minus(period);
        long prevRevenue = sumRevenueInRange(prevFrom, prevTo, category, status);
        Double growthPercent = prevRevenue == 0 ? null : ((totalRevenue - prevRevenue) * 100.0) / prevRevenue;

        Map<Long, BookAgg> bookAgg = orders.stream()
                .flatMap(o -> o.getOrderDetails().stream())
                .filter(i -> category == null || category.isBlank() || category.equalsIgnoreCase(i.getBook().getCategory()))
                .collect(Collectors.groupingBy(
                        i -> i.getBook().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            OrderDetail first = list.get(0);
                            long qty = list.stream().mapToLong(OrderDetail::getQuantity).sum();
                            return new BookAgg(first.getBook().getId(), first.getBook().getTitle(), first.getBook().getCategory(), qty);
                        })
                ));

        long totalBooksSold = bookAgg.values().stream().mapToLong(BookAgg::quantitySold).sum();
        List<BookAgg> topBooks = bookAgg.values().stream()
                .sorted(Comparator.comparingLong(BookAgg::quantitySold).reversed())
                .limit(10)
                .toList();

        String message = totalOrders == 0 ? "Không có dữ liệu bán hàng phù hợp với tiêu chí tìm kiếm" : null;
        return new SalesReportData(from, to, totalOrders, totalRevenue, prevRevenue, growthPercent, totalBooksSold, topBooks, message);
    }

    public List<Order> getSalesData(Instant fromDate, Instant toDate, String category, String orderStatus) {
        return orderRepository.findByOrderedAtBetween(fromDate, toDate).stream()
                .filter(o -> matchesSaleStatus(o, orderStatus))
                .filter(o -> matchesCategory(o, category))
                .toList();
    }

    private boolean matchesSaleStatus(Order o, String orderStatusFilter) {
        if (orderStatusFilter == null || orderStatusFilter.isBlank()) {
            return o.getStatus() == OrderStatus.DELIVERED;
        }
        return o.getStatus().name().equalsIgnoreCase(orderStatusFilter);
    }

    private boolean matchesCategory(Order o, String category) {
        if (category == null || category.isBlank()) {
            return true;
        }
        return o.getOrderDetails().stream()
                .anyMatch(i -> category.equalsIgnoreCase(i.getBook().getCategory()));
    }

    private long sumRevenueInRange(Instant from, Instant to, String category, String status) {
        return orderRepository.findByOrderedAtBetween(from, to).stream()
                .filter(o -> matchesSaleStatus(o, status))
                .filter(o -> matchesCategory(o, category))
                .mapToLong(Order::getTotalAmount)
                .sum();
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
                row.createCell(0).setCellValue(b.bookId());
                row.createCell(1).setCellValue(b.title());
                row.createCell(2).setCellValue(b.category() == null ? "" : b.category());
                row.createCell(3).setCellValue(b.quantitySold());
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
            Document doc = new Document(PageSize.A4, 40, 40, 48, 48);
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("Báo cáo bán hàng"));
            doc.add(new Paragraph("Từ: " + data.from()));
            doc.add(new Paragraph("Đến: " + data.to()));
            doc.add(new Paragraph("Tổng đơn: " + data.totalOrders()));
            doc.add(new Paragraph("Tổng doanh thu (kỳ này): " + data.totalRevenue()));
            doc.add(new Paragraph("Doanh thu kỳ trước: " + data.prevRevenue()));
            doc.add(new Paragraph("Tăng trưởng %: " + (data.growthPercent() == null ? "N/A" : data.growthPercent())));
            doc.add(new Paragraph("Tổng sách đã bán (cuốn): " + data.totalBooksSold()));
            if (data.message() != null) {
                doc.add(new Paragraph("Thông báo: " + data.message()));
            }
            doc.add(new Paragraph("Chi tiết top sách:"));
            for (BookAgg b : data.topBooks()) {
                doc.add(new Paragraph("- " + b.title() + " (" + b.bookId() + "): " + b.quantitySold()));
            }

            if (data.totalOrders() > 0 && ReportPdfChartHelper.hasChartableBooks(data.topBooks())) {
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph("Biểu đồ (PNG nhúng trong PDF):"));
                addPdfChartImage(doc, ReportPdfChartHelper.topBooksBarPng(data));
                doc.add(new Paragraph(" "));
                addPdfChartImage(doc, ReportPdfChartHelper.revenueCompareBarPng(data));
                doc.add(new Paragraph(" "));
                addPdfChartImage(doc, ReportPdfChartHelper.categoryShareRingPng(data));
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Trích xuất file thất bại. Vui lòng chia nhỏ khoảng thời gian hoặc thử lại sau");
        }
    }

    private static void addPdfChartImage(Document doc, byte[] pngBytes) throws DocumentException, IOException {
        Image img = Image.getInstance(pngBytes);
        float maxW = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
        float maxH = 220f;
        img.scaleToFit(maxW, maxH);
        doc.add(img);
    }

    private void validateRange(Instant from, Instant to) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("Khoảng thời gian không hợp lệ");
        }
        Duration d = Duration.between(from, to);
        if (d.toDays() > 366) {
            throw new IllegalArgumentException("Khoảng thời gian truy xuất vượt quá giới hạn cho phép. Vui lòng chọn khoảng thời gian tối đa là 12 tháng hoặc sử dụng tính năng Xuất báo cáo qua Email");
        }
    }

    // Named like in sequence diagram
    public byte[] generateFile(SalesReportData data, String fileFormat) {
        String f = fileFormat == null ? "" : fileFormat.toLowerCase();
        return switch (f) {
            case "xlsx" -> exportXlsx(data);
            case "pdf" -> exportPdf(data);
            default -> throw new IllegalArgumentException("format must be xlsx or pdf");
        };
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

    public record BookAgg(Long bookId, String title, String category, long quantitySold) {
    }

    public record ReportOptions(List<String> orderStatuses, List<String> categories, String defaultOrderStatus) {
    }
}

