package com.paulo.smartpet.service;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import com.paulo.smartpet.dto.SaleResponse;
import com.paulo.smartpet.dto.SalesHistorySummaryResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font TEXT_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_BOLD_FONT = new Font(Font.HELVETICA, 9, Font.BOLD);

    private final SaleService saleService;

    public ReportService(SaleService saleService) {
        this.saleService = saleService;
    }

    public byte[] generateSalesReportPdf(Long customerId, String status, LocalDate startDate, LocalDate endDate) {
        List<SaleResponse> sales = saleService.list(customerId, status, startDate, endDate);
        SalesHistorySummaryResponse summary = saleService.getHistorySummary(customerId, status, startDate, endDate);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            addTitle(document);
            addFilters(document, customerId, status, startDate, endDate);
            addSummary(document, summary);
            addSalesTable(document, sales);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException ex) {
            throw new RuntimeException("Erro ao gerar PDF do relatório de vendas", ex);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível gerar o relatório PDF de vendas", ex);
        }
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Smart Pet - Relatório de Vendas", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(12f);
        document.add(title);
    }

    private void addFilters(Document document, Long customerId, String status, LocalDate startDate, LocalDate endDate)
            throws DocumentException {

        Paragraph subtitle = new Paragraph("Filtros aplicados", SUBTITLE_FONT);
        subtitle.setSpacingAfter(6f);
        document.add(subtitle);

        document.add(new Paragraph("Cliente ID: " + (customerId == null ? "Todos" : customerId), TEXT_FONT));
        document.add(new Paragraph("Status: " + (status == null || status.isBlank() ? "Todos" : status), TEXT_FONT));
        document.add(new Paragraph("Data inicial: " + formatDate(startDate), TEXT_FONT));
        document.add(new Paragraph("Data final: " + formatDate(endDate), TEXT_FONT));

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(6f);
        document.add(spacer);
    }

    private void addSummary(Document document, SalesHistorySummaryResponse summary) throws DocumentException {
        Paragraph subtitle = new Paragraph("Resumo consolidado", SUBTITLE_FONT);
        subtitle.setSpacingAfter(6f);
        document.add(subtitle);

        document.add(new Paragraph("Quantidade de vendas: " + summary.salesCount(), TEXT_FONT));
        document.add(new Paragraph("Quantidade total de itens: " + summary.itemsCount(), TEXT_FONT));
        document.add(new Paragraph("Vendas concluídas: " + summary.completedSales(), TEXT_FONT));
        document.add(new Paragraph("Vendas canceladas: " + summary.canceledSales(), TEXT_FONT));
        document.add(new Paragraph("Total bruto: R$ " + money(summary.grossAmount()), TEXT_FONT));
        document.add(new Paragraph("Total de descontos: R$ " + money(summary.discountAmount()), TEXT_FONT));
        document.add(new Paragraph("Total líquido: R$ " + money(summary.netAmount()), TEXT_FONT));

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(8f);
        document.add(spacer);
    }

    private void addSalesTable(Document document, List<SaleResponse> sales) throws DocumentException {
        Paragraph subtitle = new Paragraph("Lista de vendas", SUBTITLE_FONT);
        subtitle.setSpacingAfter(6f);
        document.add(subtitle);

        PdfPTable table = new PdfPTable(new float[]{1.0f, 2.0f, 2.2f, 1.6f, 1.4f, 1.6f, 1.6f, 1.2f});
        table.setWidthPercentage(100);

        addHeaderCell(table, "ID");
        addHeaderCell(table, "Data");
        addHeaderCell(table, "Cliente");
        addHeaderCell(table, "Pagamento");
        addHeaderCell(table, "Itens");
        addHeaderCell(table, "Bruto");
        addHeaderCell(table, "Líquido");
        addHeaderCell(table, "Status");

        if (sales.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("Nenhuma venda encontrada para os filtros informados.", TEXT_FONT));
            emptyCell.setColspan(8);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(8f);
            table.addCell(emptyCell);
        } else {
            for (SaleResponse sale : sales) {
                table.addCell(createBodyCell(String.valueOf(sale.id())));
                table.addCell(createBodyCell(sale.saleDate() == null ? "-" : sale.saleDate().format(DATE_TIME_FORMAT)));
                table.addCell(createBodyCell(sale.customer() == null ? "Sem cliente" : sale.customer().name()));
                table.addCell(createBodyCell(sale.paymentMethod() == null ? "-" : sale.paymentMethod()));
                table.addCell(createBodyCell(String.valueOf(sale.itemsCount())));
                table.addCell(createBodyCell("R$ " + money(sale.totalAmount())));
                table.addCell(createBodyCell("R$ " + money(sale.finalAmount())));
                table.addCell(createBodyCell(sale.status() == null ? "-" : sale.status()));
            }
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SMALL_BOLD_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private PdfPCell createBodyCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TEXT_FONT));
        cell.setPadding(5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "Todas" : date.format(DATE_FORMAT);
    }

    private String money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).toPlainString();
    }
}