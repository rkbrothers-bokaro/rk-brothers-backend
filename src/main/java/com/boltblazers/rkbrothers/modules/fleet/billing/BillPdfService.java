package com.boltblazers.rkbrothers.modules.fleet.billing;

import com.boltblazers.rkbrothers.modules.fleet.billing.dto.BillLineItemDto;
import com.boltblazers.rkbrothers.modules.fleet.billing.dto.BillSummaryDto;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Renders a BillSummaryDto with the classic iText 5 API. Note: the base-14
 * Helvetica font (WinAnsi/CP1252 encoding) does not include the Rupee sign
 * (U+20B9, added to Unicode in 2010 — well after CP1252 was frozen), so
 * amounts are labelled "Rs." here rather than "₹" to avoid missing-glyph
 * rendering. The API/JSON layer is unaffected.
 */
@Service
public class BillPdfService {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

    public byte[] generatePdf(BillSummaryDto bill) {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, bill);
            addLineItemsTable(document, bill);
            addFooter(document, bill);

            document.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate bill PDF", e);
        }

        return out.toByteArray();
    }

    private void addHeader(Document document, BillSummaryDto bill) throws DocumentException {
        Paragraph title = new Paragraph("M/S R.K. Brothers", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("VEHICLE HIRE BILL", SUBTITLE_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(14);
        document.add(subtitle);

        String monthYear = Month.of(bill.month()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + bill.year();

        document.add(new Paragraph("Party Name: " + orDash(bill.partyName()), NORMAL_FONT));
        document.add(new Paragraph("Work Order No: " + orDash(bill.woNumber()), NORMAL_FONT));
        document.add(new Paragraph("Site Location: " + orDash(bill.siteLocation()), NORMAL_FONT));
        document.add(new Paragraph("Month/Year: " + monthYear, NORMAL_FONT));

        Paragraph generated = new Paragraph(
                "Generated Date: " + bill.generatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                NORMAL_FONT);
        generated.setSpacingAfter(16);
        document.add(generated);
    }

    private void addLineItemsTable(Document document, BillSummaryDto bill) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.6f, 1.4f, 1f, 1.2f, 1f, 1.2f, 1f, 1.2f});

        for (String header : new String[]{"S.No", "Vehicle No", "Type", "Billing Basis", "Quantity", "Unit", "Rate (Rs.)", "Amount (Rs.)"}) {
            table.addCell(headerCell(header));
        }

        int serialNo = 1;
        for (BillLineItemDto item : bill.lineItems()) {
            table.addCell(dataCell(String.valueOf(serialNo++), Element.ALIGN_CENTER));
            table.addCell(dataCell(orDash(item.vehicleNo()), Element.ALIGN_LEFT));
            table.addCell(dataCell(orDash(item.vehicleType()), Element.ALIGN_LEFT));
            table.addCell(dataCell(orDash(item.billingBasis()), Element.ALIGN_LEFT));
            table.addCell(dataCell(item.quantity().setScale(2, RoundingMode.HALF_UP).toPlainString(), Element.ALIGN_RIGHT));
            table.addCell(dataCell(orDash(item.unit()), Element.ALIGN_LEFT));
            table.addCell(dataCell(item.rate().setScale(2, RoundingMode.HALF_UP).toPlainString(), Element.ALIGN_RIGHT));
            table.addCell(dataCell(item.amount().setScale(2, RoundingMode.HALF_UP).toPlainString(), Element.ALIGN_RIGHT));
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", BOLD_FONT));
        totalLabel.setColspan(7);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setPadding(6);
        table.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase(bill.totalAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(), BOLD_FONT));
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValue.setPadding(6);
        table.addCell(totalValue);

        document.add(table);
    }

    private void addFooter(Document document, BillSummaryDto bill) throws DocumentException {
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingBefore(10);
        document.add(spacer);

        document.add(new Paragraph("Amount in words: " + AmountInWordsUtil.convert(bill.totalAmount()), NORMAL_FONT));

        Paragraph signatureSpace = new Paragraph(" ");
        signatureSpace.setSpacingBefore(40);
        document.add(signatureSpace);

        Paragraph signature = new Paragraph("Authorized Signatory", NORMAL_FONT);
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BOLD_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell dataCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    private String orDash(String value) {
        return value != null ? value : "-";
    }
}
