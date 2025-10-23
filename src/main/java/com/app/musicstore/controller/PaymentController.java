package com.app.musicstore.controller;

import com.app.musicstore.model.UnifiedPayment;
import com.app.musicstore.model.User;
import com.app.musicstore.service.SessionUserService;
import com.app.musicstore.service.UnifiedPaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/payments")
public class PaymentController {

        private final UnifiedPaymentService unifiedPaymentService;
        private final SessionUserService sessionUserService;

        public PaymentController(UnifiedPaymentService unifiedPaymentService, SessionUserService sessionUserService) {
                this.unifiedPaymentService = unifiedPaymentService;
                this.sessionUserService = sessionUserService;
        }

        // GET /payments/checkout?itemType=SONG&itemId=1&itemName=...&amount=...
        @GetMapping("/checkout")
        public String checkout(@RequestParam String itemType,
                        @RequestParam Long itemId,
                        @RequestParam String itemName,
                        @RequestParam Double amount,
                        @RequestParam(required = false) String successRedirect,
                        Model model,
                        HttpSession session) {
                User user = sessionUserService.getAuthenticatedUser(session);
                if (user == null)
                        return "redirect:/users/login?error=loginRequired";
                model.addAttribute("user", user);
                model.addAttribute("itemType", itemType);
                model.addAttribute("itemId", itemId);
                model.addAttribute("itemName", itemName);
                model.addAttribute("amount", amount);
                if (successRedirect != null) {
                        model.addAttribute("successRedirect", successRedirect);
                }
                return "payment_checkout";
        }

        // POST from checkout → process mock payment
        @PostMapping("/pay")
        public String pay(@RequestParam String itemType,
                        @RequestParam Long itemId,
                        @RequestParam String itemName,
                        @RequestParam Double amount,
                        @RequestParam(required = false) String successRedirect,
                        HttpSession session) {
                User user = sessionUserService.getAuthenticatedUser(session);
                if (user == null)
                        return "redirect:/users/login?error=loginRequired";
                UnifiedPayment payment = unifiedPaymentService.processMockPayment(user, itemType, itemId, itemName,
                                amount);
                if (successRedirect != null && successRedirect.startsWith("/")) {
                        String redirectUrl = successRedirect + (successRedirect.contains("?") ? "&" : "?")
                                        + "unifiedPaymentId="
                                        + payment.getId();
                        return "redirect:" + redirectUrl;
                }
                return "redirect:/payments/bill/" + payment.getId();
        }

        // Bill page
        @GetMapping("/bill/{id}")
        public String bill(@PathVariable Long id, Model model, HttpSession session) {
                User user = sessionUserService.getAuthenticatedUser(session);
                if (user == null)
                        return "redirect:/users/login?error=loginRequired";
                UnifiedPayment payment = unifiedPaymentService.getById(id);
                if (payment == null || !payment.getUser().getUserId().equals(user.getUserId())) {
                        return "redirect:/payments/history?error=notFound";
                }
                model.addAttribute("payment", payment);
                return "payment_bill";
        }

        // Payment history
        @GetMapping("/history")
        public String history(Model model, HttpSession session) {
                User user = sessionUserService.getAuthenticatedUser(session);
                if (user == null)
                        return "redirect:/users/login?error=loginRequired";
                model.addAttribute("payments", unifiedPaymentService.getUserPayments(user));
                return "payment_history";
        }

        // Download bill as PDF
        @GetMapping("/bill/{id}/download")
        public void downloadBill(@PathVariable Long id, jakarta.servlet.http.HttpServletResponse response,
                        HttpSession session) {
                try {
                        User user = sessionUserService.getAuthenticatedUser(session);
                        if (user == null) {
                                response.sendError(401);
                                return;
                        }
                        UnifiedPayment payment = unifiedPaymentService.getById(id);
                        if (payment == null || !payment.getUser().getUserId().equals(user.getUserId())) {
                                response.sendError(404);
                                return;
                        }

                        response.setContentType("application/pdf");
                        response.setHeader("Content-Disposition",
                                        "attachment; filename=tunewave-receipt-" + payment.getTransactionId() + ".pdf");

                        // Generate modern PDF bill
                        byte[] pdfBytes = generateModernPdfBill(payment);
                        response.getOutputStream().write(pdfBytes);
                        response.getOutputStream().flush();

                } catch (Exception e) {
                        try {
                                response.sendError(500);
                        } catch (Exception ignored) {
                        }
                }
        }

        private byte[] generateModernPdfBill(UnifiedPayment payment) throws Exception {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4);
                com.lowagie.text.pdf.PdfWriter writer = com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);

                document.open();

                // Custom font setup
                com.lowagie.text.pdf.BaseFont baseFont = com.lowagie.text.pdf.BaseFont.createFont(
                                com.lowagie.text.pdf.BaseFont.HELVETICA,
                                com.lowagie.text.pdf.BaseFont.WINANSI,
                                com.lowagie.text.pdf.BaseFont.EMBEDDED);

                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(baseFont, 24, com.lowagie.text.Font.BOLD,
                                new java.awt.Color(0, 212, 255));
                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(baseFont, 16, com.lowagie.text.Font.BOLD,
                                java.awt.Color.WHITE);
                com.lowagie.text.Font sectionFont = new com.lowagie.text.Font(baseFont, 14, com.lowagie.text.Font.BOLD,
                                new java.awt.Color(15, 15, 35));
                com.lowagie.text.Font labelFont = new com.lowagie.text.Font(baseFont, 11, com.lowagie.text.Font.BOLD,
                                new java.awt.Color(102, 102, 102));
                com.lowagie.text.Font valueFont = new com.lowagie.text.Font(baseFont, 11, com.lowagie.text.Font.NORMAL,
                                new java.awt.Color(51, 51, 51));
                com.lowagie.text.Font amountFont = new com.lowagie.text.Font(baseFont, 20, com.lowagie.text.Font.BOLD,
                                new java.awt.Color(15, 15, 35));
                com.lowagie.text.Font statusFont = new com.lowagie.text.Font(baseFont, 12, com.lowagie.text.Font.BOLD,
                                java.awt.Color.WHITE);
                com.lowagie.text.Font footerFont = new com.lowagie.text.Font(baseFont, 10, com.lowagie.text.Font.NORMAL,
                                new java.awt.Color(153, 153, 153));

                // Header with gradient background effect
                com.lowagie.text.pdf.PdfPTable headerTable = new com.lowagie.text.pdf.PdfPTable(1);
                headerTable.setWidthPercentage(100);
                headerTable.setSpacingAfter(25f);

                com.lowagie.text.pdf.PdfPCell headerCell = new com.lowagie.text.pdf.PdfPCell();
                headerCell.setBackgroundColor(new java.awt.Color(15, 15, 35));
                headerCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                headerCell.setPadding(25f);

                // Logo and title
                com.lowagie.text.Paragraph logo = new com.lowagie.text.Paragraph("🎵 TuneWave", titleFont);
                logo.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                headerCell.addElement(logo);

                com.lowagie.text.Paragraph receiptTitle = new com.lowagie.text.Paragraph("PAYMENT RECEIPT", headerFont);
                receiptTitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                receiptTitle.setSpacingBefore(8f);
                headerCell.addElement(receiptTitle);

                com.lowagie.text.Paragraph receiptSubtitle = new com.lowagie.text.Paragraph(
                                "Official Payment Confirmation",
                                new com.lowagie.text.Font(baseFont, 12, com.lowagie.text.Font.NORMAL,
                                                new java.awt.Color(200, 200, 200)));
                receiptSubtitle.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                receiptSubtitle.setSpacingBefore(5f);
                headerCell.addElement(receiptSubtitle);

                headerTable.addCell(headerCell);
                document.add(headerTable);

                // Transaction details section
                com.lowagie.text.pdf.PdfPTable detailsTable = new com.lowagie.text.pdf.PdfPTable(2);
                detailsTable.setWidthPercentage(100);
                detailsTable.setSpacingAfter(20f);
                detailsTable.setWidths(new float[] { 1f, 2f });

                // Transaction ID
                com.lowagie.text.pdf.PdfPCell transactionLabelCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph("Transaction ID:", labelFont));
                transactionLabelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                transactionLabelCell.setPadding(8f);
                detailsTable.addCell(transactionLabelCell);

                com.lowagie.text.pdf.PdfPCell transactionValueCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph(payment.getTransactionId(), valueFont));
                transactionValueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                transactionValueCell.setPadding(8f);
                detailsTable.addCell(transactionValueCell);

                // Payment Date
                com.lowagie.text.pdf.PdfPCell dateLabelCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph("Payment Date:", labelFont));
                dateLabelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                dateLabelCell.setPadding(8f);
                detailsTable.addCell(dateLabelCell);

                String formattedDate = payment.getPaymentDate()
                                .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm"));
                com.lowagie.text.pdf.PdfPCell dateValueCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph(formattedDate, valueFont));
                dateValueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                dateValueCell.setPadding(8f);
                detailsTable.addCell(dateValueCell);

                // Customer Name
                com.lowagie.text.pdf.PdfPCell customerLabelCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph("Customer:", labelFont));
                customerLabelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                customerLabelCell.setPadding(8f);
                detailsTable.addCell(customerLabelCell);

                com.lowagie.text.pdf.PdfPCell customerValueCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph(payment.getUser().getName(), valueFont));
                customerValueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                customerValueCell.setPadding(8f);
                detailsTable.addCell(customerValueCell);

                // Customer Email
                com.lowagie.text.pdf.PdfPCell emailLabelCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph("Email:", labelFont));
                emailLabelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                emailLabelCell.setPadding(8f);
                detailsTable.addCell(emailLabelCell);

                com.lowagie.text.pdf.PdfPCell emailValueCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph(payment.getUser().getEmail(), valueFont));
                emailValueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                emailValueCell.setPadding(8f);
                detailsTable.addCell(emailValueCell);

                document.add(detailsTable);

                // Item details section
                com.lowagie.text.pdf.PdfPTable itemTable = new com.lowagie.text.pdf.PdfPTable(1);
                itemTable.setWidthPercentage(100);
                itemTable.setSpacingAfter(20f);

                com.lowagie.text.pdf.PdfPCell itemHeaderCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph("ITEM DETAILS", sectionFont));
                itemHeaderCell.setBackgroundColor(new java.awt.Color(240, 240, 240));
                itemHeaderCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                itemHeaderCell.setPadding(12f);
                itemTable.addCell(itemHeaderCell);

                com.lowagie.text.pdf.PdfPTable itemDetailsTable = new com.lowagie.text.pdf.PdfPTable(2);
                itemDetailsTable.setWidthPercentage(100);
                itemDetailsTable.setWidths(new float[] { 1f, 2f });

                // Item Type
                com.lowagie.text.pdf.PdfPCell itemTypeLabelCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph("Type:", labelFont));
                itemTypeLabelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                itemTypeLabelCell.setPadding(8f);
                itemDetailsTable.addCell(itemTypeLabelCell);

                com.lowagie.text.pdf.PdfPCell itemTypeValueCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph(payment.getItemType(), valueFont));
                itemTypeValueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                itemTypeValueCell.setPadding(8f);
                itemDetailsTable.addCell(itemTypeValueCell);

                // Item Name
                com.lowagie.text.pdf.PdfPCell itemNameLabelCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph("Item:", labelFont));
                itemNameLabelCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                itemNameLabelCell.setPadding(8f);
                itemDetailsTable.addCell(itemNameLabelCell);

                com.lowagie.text.pdf.PdfPCell itemNameValueCell = new com.lowagie.text.pdf.PdfPCell(
                                new com.lowagie.text.Paragraph(payment.getItemName(), valueFont));
                itemNameValueCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                itemNameValueCell.setPadding(8f);
                itemDetailsTable.addCell(itemNameValueCell);

                itemTable.addCell(new com.lowagie.text.pdf.PdfPCell(itemDetailsTable));
                document.add(itemTable);

                // Amount section
                com.lowagie.text.pdf.PdfPTable amountTable = new com.lowagie.text.pdf.PdfPTable(1);
                amountTable.setWidthPercentage(100);
                amountTable.setSpacingAfter(20f);

                com.lowagie.text.pdf.PdfPCell amountCell = new com.lowagie.text.pdf.PdfPCell();
                amountCell.setBackgroundColor(new java.awt.Color(0, 212, 255));
                amountCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                amountCell.setPadding(20f);

                com.lowagie.text.Paragraph totalLabel = new com.lowagie.text.Paragraph("TOTAL AMOUNT",
                                new com.lowagie.text.Font(baseFont, 12, com.lowagie.text.Font.BOLD,
                                                java.awt.Color.WHITE));
                totalLabel.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                amountCell.addElement(totalLabel);

                com.lowagie.text.Paragraph totalAmount = new com.lowagie.text.Paragraph(
                                "LKR " + String.format("%.2f", payment.getAmount()), amountFont);
                totalAmount.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                totalAmount.setSpacingBefore(5f);
                amountCell.addElement(totalAmount);

                amountTable.addCell(amountCell);
                document.add(amountTable);

                // Status section
                com.lowagie.text.pdf.PdfPTable statusTable = new com.lowagie.text.pdf.PdfPTable(1);
                statusTable.setWidthPercentage(100);
                statusTable.setSpacingAfter(20f);

                com.lowagie.text.pdf.PdfPCell statusCell = new com.lowagie.text.pdf.PdfPCell();
                statusCell.setBackgroundColor(getStatusColor(payment.getStatus()));
                statusCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                statusCell.setPadding(15f);

                com.lowagie.text.Paragraph statusText = new com.lowagie.text.Paragraph(
                                payment.getStatus().toUpperCase(),
                                statusFont);
                statusText.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                statusCell.addElement(statusText);

                statusTable.addCell(statusCell);
                document.add(statusTable);

                // Footer
                com.lowagie.text.pdf.PdfPTable footerTable = new com.lowagie.text.pdf.PdfPTable(1);
                footerTable.setWidthPercentage(100);

                com.lowagie.text.pdf.PdfPCell footerCell = new com.lowagie.text.pdf.PdfPCell();
                footerCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
                footerCell.setPadding(15f);

                com.lowagie.text.Paragraph footerText = new com.lowagie.text.Paragraph("Thank you for your business!",
                                footerFont);
                footerText.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                footerCell.addElement(footerText);

                com.lowagie.text.Paragraph footerSubtext = new com.lowagie.text.Paragraph(
                                "This is an official receipt from TuneWave Music Store", footerFont);
                footerSubtext.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                footerSubtext.setSpacingBefore(5f);
                footerCell.addElement(footerSubtext);

                footerTable.addCell(footerCell);
                document.add(footerTable);

                document.close();
                return baos.toByteArray();
        }

        private java.awt.Color getStatusColor(String status) {
                switch (status.toUpperCase()) {
                        case "SUCCESS":
                                return new java.awt.Color(76, 175, 80); // Green
                        case "COMPLETED":
                                return new java.awt.Color(76, 175, 80); // Green
                        case "PENDING":
                                return new java.awt.Color(255, 152, 0); // Orange
                        case "PROCESSING":
                                return new java.awt.Color(255, 152, 0); // Orange
                        case "FAILED":
                                return new java.awt.Color(244, 67, 54); // Red
                        case "CANCELLED":
                                return new java.awt.Color(244, 67, 54); // Red
                        case "REFUNDED":
                                return new java.awt.Color(156, 39, 176); // Purple
                        default:
                                return new java.awt.Color(33, 150, 243); // Blue
                }
        }
}