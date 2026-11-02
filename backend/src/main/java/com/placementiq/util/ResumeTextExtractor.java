package com.placementiq.util;
 
import com.placementiq.entity.Resume;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
 
import java.io.ByteArrayInputStream;
 
/**
 * Extracts plain text from an uploaded resume document.
 *
 * Two libraries are used, both added to pom.xml in this part:
 *  - Apache PDFBox  -> reads the text layer of a .pdf file
 *  - Apache POI (poi-ooxml) -> reads the paragraphs of a .docx (OOXML) file
 *
 * Extraction is best-effort on purpose: a scanned/image-only PDF has no text
 * layer, and a corrupt document should not prevent the user from storing their
 * file. In those cases an empty string is returned and the upload still
 * succeeds, with the API response reporting that extraction produced no text.
 * The extracted text is stored so that the ATS analysis and job-description
 * matching modules in later parts do not have to re-read the file.
 */
@Component
public class ResumeTextExtractor {
 
    private static final Logger log = LoggerFactory.getLogger(ResumeTextExtractor.class);
 
    /** Upper bound so an unusually long document cannot bloat the database row. */
    private static final int MAX_CHARACTERS = 50_000;
 
    /**
     * @param content  raw bytes of the uploaded document
     * @param fileType PDF or DOCX
     * @return the extracted, normalised text, or an empty string if none could be read
     */
    public String extract(byte[] content, Resume.FileType fileType) {
        if (content == null || content.length == 0) {
            return "";
        }
        try {
            String raw = (fileType == Resume.FileType.PDF)
                    ? extractFromPdf(content)
                    : extractFromDocx(content);
            return normalize(raw);
        } catch (Exception ex) {
            log.warn("Text extraction failed for a {} resume: {}", fileType, ex.getMessage());
            return "";
        }
    }
 
    private String extractFromPdf(byte[] content) throws Exception {
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }
 
    private String extractFromDocx(byte[] content) throws Exception {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
 
    /**
     * Collapses Windows line endings and runs of blank lines / spaces so the
     * stored text is compact and predictable for later AI processing.
     */
    private String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
 
        if (cleaned.length() > MAX_CHARACTERS) {
            cleaned = cleaned.substring(0, MAX_CHARACTERS);
        }
        return cleaned;
    }
}
