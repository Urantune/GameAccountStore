package webBackEnd.controller.Staff;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import webBackEnd.entity.Transaction;
import webBackEnd.service.TransactionService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/staffHome")
public class StaffRevenueController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/revenue")
    public String revenue(Model model) {
        List<Transaction> transactions = transactionService.getAll();
        transactions.sort(Comparator.comparing(Transaction::getDateCreated,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;
        Map<String, BigDecimal> revenueByDate = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            if (t == null || t.getAmount() == null || t.getDateCreated() == null) continue;

            if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) totalIncome = totalIncome.add(t.getAmount());
            else if (t.getAmount().compareTo(BigDecimal.ZERO) < 0) totalRefund = totalRefund.add(t.getAmount().abs());

            String dateKey = t.getDateCreated().toLocalDate().toString(); // yyyy-MM-dd
            revenueByDate.putIfAbsent(dateKey, BigDecimal.ZERO);
            revenueByDate.put(dateKey, revenueByDate.get(dateKey).add(t.getAmount()));
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalRefund", totalRefund);
        model.addAttribute("netRevenue", totalIncome.subtract(totalRefund));

        model.addAttribute("revenueLabels", new ArrayList<>(revenueByDate.keySet()));
        model.addAttribute("revenueData", revenueByDate.values().stream()
                .map(v -> v == null ? 0.0 : v.doubleValue())
                .toList());

        return "staff/Revenue";
    }

    @GetMapping("/revenue/export/excel")
    public void exportRevenueExcel(HttpServletResponse response) throws IOException {

        List<Transaction> transactions = transactionService.getAll();
        transactions.sort(Comparator.comparing(Transaction::getDateCreated,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;

        Map<String, BigDecimal> incomeByDate = new LinkedHashMap<>();
        Map<String, BigDecimal> refundByDate = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            if (t == null || t.getAmount() == null || t.getDateCreated() == null) continue;

            String dateKey = t.getDateCreated().toLocalDate().toString();

            if (t.getAmount().signum() > 0) {
                totalIncome = totalIncome.add(t.getAmount());
                incomeByDate.merge(dateKey, t.getAmount(), BigDecimal::add);
            } else {
                totalRefund = totalRefund.add(t.getAmount().abs());
                refundByDate.merge(dateKey, t.getAmount().abs(), BigDecimal::add);
            }
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Revenue");

        /* ================= STYLE ================= */
        DataFormat df = workbook.createDataFormat();

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 18);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(df.getFormat("#,##0 \"đ\""));

        CellStyle incomeStyle = workbook.createCellStyle();
        incomeStyle.cloneStyleFrom(moneyStyle);
        Font incomeFont = workbook.createFont();
        incomeFont.setColor(IndexedColors.GREEN.getIndex());
        incomeStyle.setFont(incomeFont);

        CellStyle refundStyle = workbook.createCellStyle();
        refundStyle.cloneStyleFrom(moneyStyle);
        Font refundFont = workbook.createFont();
        refundFont.setColor(IndexedColors.RED.getIndex());
        refundStyle.setFont(refundFont);

        int rowIdx = 0;

        /* ================= TITLE ================= */
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REVENUE REPORT");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        rowIdx++;

        rowIdx = writeSummaryRow(sheet, rowIdx, "Total Income", totalIncome, moneyStyle);
        rowIdx = writeSummaryRow(sheet, rowIdx, "Total Refund", totalRefund, moneyStyle);
        rowIdx = writeSummaryRow(sheet, rowIdx, "Net Revenue",
                totalIncome.subtract(totalRefund), moneyStyle);

        rowIdx += 2;

        /* ================= TABLE ================= */
        Row headerRow = sheet.createRow(rowIdx++);
        String[] headers = {"Customer", "Description", "Amount", "Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.createFreezePane(0, rowIdx);

        for (Transaction t : transactions) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(
                    t.getCustomer() != null ? t.getCustomer().getUsername() : "System"
            );
            row.createCell(1).setCellValue(t.getDescription());

            Cell amountCell = row.createCell(2);
            amountCell.setCellValue(t.getAmount().doubleValue());
            amountCell.setCellStyle(
                    t.getAmount().signum() >= 0 ? incomeStyle : refundStyle
            );

            row.createCell(3).setCellValue(
                    t.getDateCreated().toLocalDate().toString()
            );
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        /* ================= CHART SHEET ================= */
        XSSFSheet chartSheet = workbook.createSheet("Chart");

        Row h = chartSheet.createRow(0);
        h.createCell(0).setCellValue("Date");
        h.createCell(1).setCellValue("Income");
        h.createCell(2).setCellValue("Refund");
        h.createCell(3).setCellValue("Net");

        int chartRow = 1;
        for (String date : incomeByDate.keySet()) {
            BigDecimal income = incomeByDate.getOrDefault(date, BigDecimal.ZERO);
            BigDecimal refund = refundByDate.getOrDefault(date, BigDecimal.ZERO);
            Row r = chartSheet.createRow(chartRow++);
            r.createCell(0).setCellValue(date);
            r.createCell(1).setCellValue(income.doubleValue());
            r.createCell(2).setCellValue(refund.doubleValue());
            r.createCell(3).setCellValue(income.subtract(refund).doubleValue());
        }

        XSSFDrawing drawing = chartSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor =
                drawing.createAnchor(0, 0, 0, 0, 5, 1, 16, 20);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Revenue Overview");
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis xAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.LEFT);

        XDDFDataSource<String> dates =
                XDDFDataSourcesFactory.fromStringCellRange(
                        chartSheet, new CellRangeAddress(1, chartRow - 1, 0, 0)
                );

        XDDFNumericalDataSource<Double> income =
                XDDFDataSourcesFactory.fromNumericCellRange(
                        chartSheet, new CellRangeAddress(1, chartRow - 1, 1, 1)
                );

        XDDFNumericalDataSource<Double> refund =
                XDDFDataSourcesFactory.fromNumericCellRange(
                        chartSheet, new CellRangeAddress(1, chartRow - 1, 2, 2)
                );

        XDDFNumericalDataSource<Double> net =
                XDDFDataSourcesFactory.fromNumericCellRange(
                        chartSheet, new CellRangeAddress(1, chartRow - 1, 3, 3)
                );

        XDDFLineChartData data =
                (XDDFLineChartData) chart.createData(ChartTypes.LINE, xAxis, yAxis);

        data.addSeries(dates, income).setTitle("Income", null);
        data.addSeries(dates, refund).setTitle("Refund", null);
        data.addSeries(dates, net).setTitle("Net Revenue", null);

        chart.plot(data);

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=revenue_report.xlsx"
        );

        workbook.write(response.getOutputStream());
        workbook.close();
    }


    private int writeSummaryRow(
            Sheet sheet, int rowIdx, String label, BigDecimal value, CellStyle style) {

        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(label);
        Cell cell = row.createCell(1);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
        return rowIdx;
    }




}
