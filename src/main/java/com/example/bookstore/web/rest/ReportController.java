package com.example.bookstore.web.rest;

import com.example.bookstore.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('MANAGER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales/options")
    public ReportService.ReportOptions showReportOptions() {
        return reportService.showReportOptions();
    }

    @GetMapping("/sales")
    public ReportService.SalesReportData viewSalesReport(
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "orderStatus", required = false) String orderStatus
    ) {
        return reportService.generateReport(Instant.parse(fromDate), Instant.parse(toDate), category, orderStatus);
    }

    @GetMapping("/sales/export")
    public ResponseEntity<byte[]> exportReportFile(
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate,
            @RequestParam("fileFormat") String fileFormat,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "orderStatus", required = false) String orderStatus
    ) {
        ReportService.SalesReportData data = reportService.generateReport(
                Instant.parse(fromDate), Instant.parse(toDate), category, orderStatus);

        String f = fileFormat == null ? "" : fileFormat.toLowerCase();
        byte[] bytes = reportService.generateFile(data, f);

        if (f.equals("xlsx")) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sales-report.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        }

        // pdf
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sales-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}

