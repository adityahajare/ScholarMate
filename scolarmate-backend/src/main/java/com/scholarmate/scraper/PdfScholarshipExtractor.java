package com.scholarmate.scraper;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

@Service
public class PdfScholarshipExtractor {

        private static final int MAX_PDF_BYTES = 10 * 1024 * 1024;

        @Value("${scholarmate.ocr.tessdata-path:C:\\Program Files\\Tesseract-OCR\\tessdata}")
        private String tessdataPath;

    public String extractText(String pdfUrl) {

        try {
                        Connection.Response response = Jsoup.connect(pdfUrl)
                                        .userAgent("Mozilla/5.0")
                                        .followRedirects(true)
                                        .ignoreContentType(true)
                                        .timeout(20000)
                                        .maxBodySize(MAX_PDF_BYTES)
                                        .execute();
                        if (response.statusCode() >= 400) {
                                throw new IllegalArgumentException("PDF server returned HTTP " + response.statusCode());
                        }
                        byte[] pdfBytes = response.bodyAsBytes();
                        if (pdfBytes.length == 0 || pdfBytes.length >= MAX_PDF_BYTES) {
                                throw new IllegalArgumentException("PDF is empty or exceeds the 10 MB limit.");
                        }
                        if (pdfBytes.length < 5 || pdfBytes[0] != '%'
                                        || pdfBytes[1] != 'P' || pdfBytes[2] != 'D'
                                        || pdfBytes[3] != 'F' || pdfBytes[4] != '-') {
                                throw new IllegalArgumentException("URL did not return a PDF document.");
                        }
            return extractText(pdfBytes);

        } catch (Exception e) {

            throw new RuntimeException(
                    "PDF extraction failed: "
                            + e.getMessage());
        }
    }

        public String extractText(byte[] pdfBytes) {
                try {
                        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                                PDFTextStripper stripper = new PDFTextStripper();
                                stripper.setStartPage(1);
                                stripper.setEndPage(document.getNumberOfPages());
                                String text = stripper.getText(document);
                                if (isValidExtractedText(text)) {
                                        return text;
                                }
                        }
                        return extractUsingOcr(pdfBytes);
                } catch (Exception e) {
                        throw new RuntimeException("PDF extraction failed: " + e.getMessage(), e);
                }
        }

        public String extractImageText(byte[] imageBytes) {
                try {
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        if (image == null) {
                                throw new IllegalArgumentException("Unsupported or invalid image file.");
                        }

                        Tesseract tesseract = new Tesseract();
                        tesseract.setDatapath(tessdataPath);
                        tesseract.setLanguage("eng");
                        String text = tesseract.doOCR(image);
                        if (!isValidExtractedText(text)) {
                                throw new IllegalArgumentException("OCR could not read enough text from the image.");
                        }
                        return text;
                } catch (TesseractException e) {
                        throw new RuntimeException("Image OCR failed: " + e.getMessage(), e);
                } catch (Exception e) {
                        throw new RuntimeException("Image OCR failed: " + e.getMessage(), e);
                }
        }

    private String extractUsingOcr(byte[] pdfBytes) {

        StringBuilder completeText =
                new StringBuilder();

        try (
                PDDocument document =
                        Loader.loadPDF(pdfBytes)
        ) {

            PDFRenderer renderer =
                    new PDFRenderer(document);

            Tesseract tesseract =
                    new Tesseract();

            /*
             * IMPORTANT:
             * Tell Tess4J exactly where
             * eng.traineddata is located.
             */
            tesseract.setDatapath(
                    tessdataPath);

            /*
             * English OCR model.
             */
            tesseract.setLanguage("eng");

            /*
             * Process EVERY page.
             */
            for (int page = 0;
                 page < document.getNumberOfPages();
                 page++) {

                System.out.println(
                        "OCR processing page "
                                + (page + 1)
                                + " of "
                                + document.getNumberOfPages());

                BufferedImage image =
                        renderer.renderImageWithDPI(
                                page,
                                300,
                                ImageType.RGB);

                String pageText =
                        tesseract.doOCR(image);

                if (pageText != null
                        && !pageText.isBlank()) {

                    completeText
                            .append("\n\n")
                            .append("===== PAGE ")
                            .append(page + 1)
                            .append(" =====\n\n")
                            .append(pageText);
                }
            }

            String finalText =
                    completeText.toString().trim();

            /*
             * Never save bad OCR output.
             */
            if (!isValidExtractedText(finalText)) {

                throw new RuntimeException(
                        "PDF OCR produced insufficient or invalid text");
            }

            /*
             * Return ALL OCR text.
             * No truncation.
             */
            return finalText;

        } catch (TesseractException e) {

            throw new RuntimeException(
                    "OCR extraction failed: "
                            + e.getMessage());

        } catch (Exception e) {

            throw new RuntimeException(
                    "OCR PDF processing failed: "
                            + e.getMessage());
        }
    }

    private boolean isValidExtractedText(String text) {

        if (text == null) {
            return false;
        }

        String cleaned =
                text.replaceAll("\\s+", " ")
                        .trim();

        /*
         * Reject extremely small/broken results.
         */
        if (cleaned.length() < 100) {
            return false;
        }

        int letterCount = 0;

        for (char c : cleaned.toCharArray()) {

            if (Character.isLetter(c)) {
                letterCount++;
            }
        }

        return letterCount >= 50;
    }
}
