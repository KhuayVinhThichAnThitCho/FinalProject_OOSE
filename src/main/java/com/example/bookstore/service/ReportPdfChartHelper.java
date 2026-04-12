package com.example.bookstore.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tạo ảnh PNG biểu đồ từ {@link ReportService.SalesReportData} để nhúng vào PDF.
 */
final class ReportPdfChartHelper {

    private static final int CHART_WIDTH = 640;
    private static final int CHART_HEIGHT = 360;

    private ReportPdfChartHelper() {
    }

    static byte[] topBooksBarPng(ReportService.SalesReportData data) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ReportService.BookAgg b : data.topBooks()) {
            String label = shorten(b.title(), 24);
            dataset.addValue(b.quantitySold(), "Đã bán (cuốn)", label);
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Top sách bán chạy",
                "Sách",
                "Số lượng",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        styleChart(chart);
        if (chart.getCategoryPlot() != null) {
            chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            chart.getCategoryPlot().getDomainAxis().setMaximumCategoryLabelLines(2);
        }
        return chartToPng(chart);
    }

    static byte[] revenueCompareBarPng(ReportService.SalesReportData data) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(data.totalRevenue(), "Doanh thu (VND)", "Kỳ này");
        dataset.addValue(data.prevRevenue(), "Doanh thu (VND)", "Kỳ trước");
        JFreeChart chart = ChartFactory.createBarChart(
                "So sánh doanh thu",
                "",
                "VND",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        styleChart(chart);
        return chartToPng(chart);
    }

    static byte[] categoryShareRingPng(ReportService.SalesReportData data) throws IOException {
        DefaultPieDataset<String> pie = new DefaultPieDataset<>();
        Map<String, Long> byCat = new LinkedHashMap<>();
        for (ReportService.BookAgg b : data.topBooks()) {
            String cat = (b.category() == null || b.category().isBlank()) ? "Không phân loại" : b.category().trim();
            byCat.merge(cat, b.quantitySold(), Long::sum);
        }
        for (Map.Entry<String, Long> e : byCat.entrySet()) {
            pie.setValue(shorten(e.getKey(), 18), e.getValue());
        }
        JFreeChart chart = ChartFactory.createRingChart(
                "Tỷ trọng SL bán theo danh mục (từ top sách)",
                pie,
                true,
                true,
                false
        );
        styleChart(chart);
        if (chart.getPlot() instanceof RingPlot ring) {
            ring.setSectionDepth(0.35);
            ring.setSeparatorsVisible(true);
            ring.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        }
        return chartToPng(chart);
    }

    private static void styleChart(JFreeChart chart) {
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setPadding(new RectangleInsets(8, 12, 8, 12));
        TextTitle t = chart.getTitle();
        if (t != null) {
            t.setFont(new Font("SansSerif", Font.BOLD, 15));
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 11));
        }
    }

    private static byte[] chartToPng(JFreeChart chart) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, CHART_WIDTH, CHART_HEIGHT);
        return baos.toByteArray();
    }

    private static String shorten(String s, int maxLen) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() <= maxLen ? t : t.substring(0, maxLen - 1) + "…";
    }

    static boolean hasChartableBooks(List<ReportService.BookAgg> topBooks) {
        return topBooks != null && !topBooks.isEmpty();
    }
}
