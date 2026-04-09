package com.example.bookstore.web.rest;

import com.example.bookstore.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public ReportService.SalesReportData sales(@RequestParam("from") String from, @RequestParam("to") String to) {
        return reportService.buildSalesReport(Instant.parse(from), Instant.parse(to));
    }

    @GetMapping("/sales/export")
    public ResponseEntity<byte[]> export(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("format") String format
    ) {
        ReportService.SalesReportData data = reportService.buildSalesReport(Instant.parse(from), Instant.parse(to));
        String f = format == null ? "" : format.toLowerCase();

        if (f.equals("xlsx")) {
            byte[] bytes = reportService.exportXlsx(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sales-report.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        }

        if (f.equals("pdf")) {
            byte[] bytes = reportService.exportPdf(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sales-report.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bytes);
        }

        throw new IllegalArgumentException("format must be xlsx or pdf");
    }
}

