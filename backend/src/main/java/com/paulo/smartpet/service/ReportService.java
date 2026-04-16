package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.SaleDetailsResponse;
import com.paulo.smartpet.dto.SaleItemResponse;
import com.paulo.smartpet.dto.SaleResponse;
import com.paulo.smartpet.dto.SalesHistorySummaryResponse;
import com.paulo.smartpet.entity.CompanySettings;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
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
    private static final Font RECEIPT_TITLE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font RECEIPT_TEXT_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);

    private final SaleService saleService;
    private final CompanySettingsService companySettingsService;

    public ReportService(SaleService saleService, CompanySettingsService companySettingsService) {
        this.saleService = saleService;
        this.companySettingsService = companySettingsService;
    }

    public byte[] generateSalesReportPdf(Long customerId, String status, LocalDate startDate, LocalDate endDate) {
        List<SaleResponse> sales = saleService.list(null, customerId, status, startDate, endDate);
SalesHistorySummaryResponse summary = saleService.getHistorySummary(null, customerId, status, startDate, endDate);
        CompanySettings company = companySettingsService.getCurrentEntity();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            addCompanyHeader(document, company);
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

    public byte[] generateSaleReceiptPdf(Long saleId) {
        SaleDetailsResponse sale = saleService.getById(saleId);
        CompanySettings company = companySettingsService.getCurrentEntity();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Rectangle pageSize = new Rectangle(226, 700);
            Document document = new Document(pageSize, 16, 16, 16, 16);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            addReceiptTitle(document, company);
            addReceiptHeader(document, sale, company);
            addReceiptItems(document, sale.items());
            addReceiptTotals(document, sale);
            addReceiptFooter(document, company);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException ex) {
            throw new RuntimeException("Erro ao gerar PDF do comprovante", ex);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível gerar o comprovante da venda", ex);
        }
    }

    private void addCompanyHeader(Document document, CompanySettings company) throws DocumentException {
        Paragraph companyName = new Paragraph(defaultText(company.getTradeName()), SUBTITLE_FONT);
        companyName.setAlignment(Element.ALIGN_CENTER);
        companyName.setSpacingAfter(4f);
        document.add(companyName);

        if (company.getLegalName() != null) {
            Paragraph legalName = new Paragraph(company.getLegalName(), TEXT_FONT);
            legalName.setAlignment(Element.ALIGN_CENTER);
            document.add(legalName);
        }

        if (company.getCnpj() != null) {
            Paragraph cnpj = new Paragraph("CNPJ: " + company.getCnpj(), TEXT_FONT);
            cnpj.setAlignment(Element.ALIGN_CENTER);
            document.add(cnpj);
        }

        if (company.getPhone() != null) {
            Paragraph phone = new Paragraph("Telefone: " + company.getPhone(), TEXT_FONT);
            phone.setAlignment(Element.ALIGN_CENTER);
            document.add(phone);
        }

        if (company.getEmail() != null) {
            Paragraph email = new Paragraph("E-mail: " + company.getEmail(), TEXT_FONT);
            email.setAlignment(Element.ALIGN_CENTER);
            document.add(email);
        }

        if (company.getAddress() != null) {
            Paragraph address = new Paragraph(company.getAddress(), TEXT_FONT);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
        }

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(6f);
        document.add(spacer);
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("Relatório de Vendas", TITLE_FONT);
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

    private void addReceiptTitle(Document document, CompanySettings company) throws DocumentException {
        Paragraph title = new Paragraph(defaultText(company.getTradeName()).toUpperCase(), RECEIPT_TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4f);
        document.add(title);

        if (company.getCnpj() != null && !company.getCnpj().isBlank()) {
            Paragraph cnpj = new Paragraph("CNPJ: " + company.getCnpj(), RECEIPT_TEXT_FONT);
            cnpj.setAlignment(Element.ALIGN_CENTER);
            cnpj.setSpacingAfter(2f);
            document.add(cnpj);
        }

        if (company.getPhone() != null && !company.getPhone().isBlank()) {
            Paragraph phone = new Paragraph("Tel: " + company.getPhone(), RECEIPT_TEXT_FONT);
            phone.setAlignment(Element.ALIGN_CENTER);
            document.add(phone);
        }

        if (company.getAddress() != null && !company.getAddress().isBlank()) {
            Paragraph address = new Paragraph(company.getAddress(), RECEIPT_TEXT_FONT);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
        }

        Paragraph subtitle = new Paragraph("Comprovante de Venda", SMALL_BOLD_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10f);
        document.add(subtitle);
    }

    private void addReceiptHeader(Document document, SaleDetailsResponse sale, CompanySettings company) throws DocumentException {
        document.add(new Paragraph("Venda: #" + sale.id(), RECEIPT_TEXT_FONT));
        document.add(new Paragraph("Data: " + (sale.saleDate() == null ? "-" : sale.saleDate().format(DATE_TIME_FORMAT)), RECEIPT_TEXT_FONT));
        document.add(new Paragraph("Pagamento: " + defaultText(sale.paymentMethod()), RECEIPT_TEXT_FONT));
        document.add(new Paragraph("Status: " + defaultText(sale.status()), RECEIPT_TEXT_FONT));

        if (sale.customer() != null) {
            document.add(new Paragraph("Cliente: " + defaultText(sale.customer().name()), RECEIPT_TEXT_FONT));
            document.add(new Paragraph("CPF: " + defaultText(sale.customer().cpf()), RECEIPT_TEXT_FONT));
        } else {
            document.add(new Paragraph("Cliente: Consumidor não identificado", RECEIPT_TEXT_FONT));
        }

        if (company.getEmail() != null && !company.getEmail().isBlank()) {
            document.add(new Paragraph("Contato: " + company.getEmail(), RECEIPT_TEXT_FONT));
        }

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(4f);
        document.add(spacer);
    }

    private void addReceiptItems(Document document, List<SaleItemResponse> items) throws DocumentException {
        Paragraph itemsTitle = new Paragraph("Itens", SMALL_BOLD_FONT);
        itemsTitle.setSpacingAfter(4f);
        document.add(itemsTitle);

        if (items == null || items.isEmpty()) {
            document.add(new Paragraph("Nenhum item encontrado.", RECEIPT_TEXT_FONT));
            return;
        }

        for (SaleItemResponse item : items) {
            document.add(new Paragraph(item.productName(), RECEIPT_TEXT_FONT));
            document.add(new Paragraph(
                    item.quantity() + " x R$ " + money(item.unitPrice()) + " = R$ " + money(item.subtotal()),
                    RECEIPT_TEXT_FONT
            ));
            document.add(new Paragraph(" ", RECEIPT_TEXT_FONT));
        }
    }

    private void addReceiptTotals(Document document, SaleDetailsResponse sale) throws DocumentException {
        Paragraph totalTitle = new Paragraph("Totais", SMALL_BOLD_FONT);
        totalTitle.setSpacingAfter(4f);
        document.add(totalTitle);

        document.add(new Paragraph("Total bruto: R$ " + money(sale.totalAmount()), RECEIPT_TEXT_FONT));
        document.add(new Paragraph("Desconto: R$ " + money(sale.discount()), RECEIPT_TEXT_FONT));
        document.add(new Paragraph("Total líquido: R$ " + money(sale.finalAmount()), SMALL_BOLD_FONT));

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(6f);
        document.add(spacer);
    }

    private void addReceiptFooter(Document document, CompanySettings company) throws DocumentException {
        Paragraph footer = new Paragraph(
                defaultText(company.getReceiptFooterMessage(), "Obrigado pela preferência!"),
                RECEIPT_TEXT_FONT
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingAfter(4f);
        document.add(footer);

        Paragraph end = new Paragraph(defaultText(company.getTradeName()), RECEIPT_TEXT_FONT);
        end.setAlignment(Element.ALIGN_CENTER);
        document.add(end);
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

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}