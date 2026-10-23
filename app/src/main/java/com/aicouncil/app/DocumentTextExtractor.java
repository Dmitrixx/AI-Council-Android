package com.aicouncil.app;

import android.content.Context;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Small, bounded, local-only text extractors for PDF, DOCX and XLSX.
 * This class never uploads source files, executes document content, or performs OCR.
 */
public final class DocumentTextExtractor {
    private static final int MAX_INPUT_BYTES = 10 * 1024 * 1024;
    private static final int MAX_EXTRACTED_CHARS = 90_000;
    private static final int MAX_PDF_PAGES = 25;
    private static final int MAX_ARCHIVE_ENTRIES = 3_000;
    private static final long MAX_ARCHIVE_INFLATED_BYTES = 30L * 1024L * 1024L;
    private static final int MAX_SINGLE_ENTRY_BYTES = 20 * 1024 * 1024;
    private static final int MAX_XML_ENTRY_BYTES = 8 * 1024 * 1024;
    private static final int MAX_STORED_XML_BYTES = 15 * 1024 * 1024;
    private static final int MAX_SHARED_TEXT_CHARS = 2_000_000;
    private static final int MAX_XLSX_SHEETS = 20;
    private static final int MAX_XLSX_ROWS_PER_SHEET = 300;
    private static final int MAX_XLSX_COLUMNS_PER_ROW = 64;
    private static final Object PDF_INIT_LOCK = new Object();
    private static boolean pdfBoxInitialized;

    private DocumentTextExtractor() { }

    public static String extract(Context context, byte[] fileBytes, String fileName) throws IOException {
        if (context == null) throw new IOException("Не удалось получить контекст Android.");
        if (fileBytes == null || fileBytes.length == 0) throw new IOException("Файл пуст.");
        if (fileBytes.length > MAX_INPUT_BYTES) throw new IOException("Размер файла превышает 10 МБ.");
        String extension = extensionOf(fileName);
        String result;
        if ("pdf".equals(extension)) {
            result = extractPdf(context, fileBytes);
            if (result.trim().isEmpty()) {
                throw new IOException("В PDF не обнаружен извлекаемый текст. Возможно, это скан или набор изображений. OCR в этой версии не реализован.");
            }
        } else if ("docx".equals(extension)) {
            result = extractDocx(fileBytes);
            if (result.trim().isEmpty()) throw new IOException("В DOCX не найден текст. Проверьте, что документ не пустой и не повреждён.");
        } else if ("xlsx".equals(extension)) {
            result = extractXlsx(fileBytes);
            if (result.trim().isEmpty()) throw new IOException("В XLSX не найден текст или значения ячеек.");
        } else {
            throw new IOException("Формат документа не поддерживается.");
        }
        return normalize(result);
    }

    private static String extensionOf(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot < 0 || dot == name.length() - 1 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String extractPdf(Context context, byte[] bytes) throws IOException {
        synchronized (PDF_INIT_LOCK) {
            if (!pdfBoxInitialized) {
                PDFBoxResourceLoader.init(context.getApplicationContext());
                pdfBoxInitialized = true;
            }
        }
        try (PDDocument document = PDDocument.load(bytes)) {
            int pages = document.getNumberOfPages();
            if (pages <= 0) throw new IOException("В PDF отсутствуют страницы.");
            int pagesToRead = Math.min(pages, MAX_PDF_PAGES);
            BoundedPdfTextStripper stripper = new BoundedPdfTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(pagesToRead);
            String extracted = stripper.getText(document);
            if (extracted == null || extracted.trim().isEmpty()) return "";
            if (pages > pagesToRead) {
                extracted = "[PDF: извлечён текст первых " + pagesToRead + " страниц из " + pages
                    + ". Остальные страницы пропущены ради ограничения ресурсов. OCR не выполнялся.]\n\n" + extracted;
            } else {
                extracted = "[PDF: страниц обработано — " + pages + ". OCR не выполнялся.]\n\n" + extracted;
            }
            return extracted;
        } catch (IOException error) {
            String message = error.getMessage();
            if (message != null && message.contains("90 000")) throw error;
            throw new IOException("Не удалось извлечь текст из PDF. Проверьте, что файл не повреждён и не защищён паролем.", error);
        } catch (RuntimeException error) {
            throw new IOException("PDF не удалось обработать на устройстве. Попробуйте документ меньшего размера.", error);
        }
    }

    private static final class BoundedPdfTextStripper extends PDFTextStripper {
        private final StringBuilder lengthCounter = new StringBuilder();

        BoundedPdfTextStripper() throws IOException {
            setSortByPosition(true);
            setLineSeparator("\n");
        }

        @Override
        protected void writeString(String text) throws IOException {
            if (text != null) {
                if (lengthCounter.length() + text.length() > MAX_EXTRACTED_CHARS) {
                    throw new IOException("Извлечённый текст PDF превышает лимит 90 000 символов. Используйте документ меньшего размера.");
                }
                lengthCounter.append(text);
            }
            super.writeString(text);
        }
    }

    private static String extractDocx(byte[] bytes) throws IOException {
        Map<String, byte[]> entries = readOfficeArchive(bytes, true);
        byte[] documentXml = entries.get("word/document.xml");
        if (documentXml == null) throw new IOException("DOCX повреждён или в нём отсутствует word/document.xml.");
        DocxHandler handler = new DocxHandler();
        parseXml(documentXml, handler, "DOCX");
        return handler.text.toString();
    }

    private static String extractXlsx(byte[] bytes) throws IOException {
        Map<String, byte[]> entries = readOfficeArchive(bytes, false);
        byte[] workbookXml = entries.get("xl/workbook.xml");
        byte[] relationshipsXml = entries.get("xl/_rels/workbook.xml.rels");
        if (workbookXml == null) throw new IOException("XLSX повреждён или в нём отсутствует xl/workbook.xml.");

        List<SheetRef> sheets = new ArrayList<>();
        WorkbookHandler workbookHandler = new WorkbookHandler(sheets);
        parseXml(workbookXml, workbookHandler, "XLSX workbook");

        Map<String, String> relationshipTargets = new LinkedHashMap<>();
        if (relationshipsXml != null) {
            RelationshipHandler relationshipHandler = new RelationshipHandler(relationshipTargets);
            parseXml(relationshipsXml, relationshipHandler, "XLSX relationships");
        }

        List<String> sharedStrings = Collections.emptyList();
        byte[] sharedXml = entries.get("xl/sharedStrings.xml");
        if (sharedXml != null) {
            SharedStringsHandler stringsHandler = new SharedStringsHandler();
            parseXml(sharedXml, stringsHandler, "XLSX shared strings");
            sharedStrings = stringsHandler.values;
        }

        List<SheetRef> orderedSheets = new ArrayList<>();
        for (SheetRef sheet : sheets) {
            String target = relationshipTargets.get(sheet.relationshipId);
            String normalizedTarget = normalizeRelationshipTarget(target);
            if (normalizedTarget != null && entries.containsKey(normalizedTarget)) {
                orderedSheets.add(new SheetRef(sheet.name, sheet.relationshipId, normalizedTarget));
            }
        }
        if (orderedSheets.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String name : entries.keySet()) {
                if (name.startsWith("xl/worksheets/") && name.endsWith(".xml")) names.add(name);
            }
            Collections.sort(names, Comparator.naturalOrder());
            for (String name : names) orderedSheets.add(new SheetRef(name.substring(name.lastIndexOf('/') + 1), "", name));
        }
        if (orderedSheets.isEmpty()) throw new IOException("В XLSX не обнаружены листы с данными.");

        StringBuilder out = new StringBuilder();
        int maxSheets = Math.min(orderedSheets.size(), MAX_XLSX_SHEETS);
        for (int i = 0; i < maxSheets; i++) {
            SheetRef sheet = orderedSheets.get(i);
            appendBounded(out, "\n\nЛист: " + safeLabel(sheet.name) + "\n");
            SheetHandler handler = new SheetHandler(sharedStrings);
            parseXml(entries.get(sheet.path), handler, "XLSX sheet");
            appendBounded(out, handler.text.toString());
            if (handler.rowsTruncated) appendBounded(out, "\n[На листе пропущены строки после лимита " + MAX_XLSX_ROWS_PER_SHEET + ".]\n");
            if (handler.columnsTruncated) appendBounded(out, "\n[Часть столбцов пропущена после лимита " + MAX_XLSX_COLUMNS_PER_ROW + ".]\n");
        }
        if (orderedSheets.size() > maxSheets) {
            appendBounded(out, "\n[Обработаны первые " + maxSheets + " листов из " + orderedSheets.size() + ".]\n");
        }
        return out.toString();
    }

    private static String normalizeRelationshipTarget(String target) {
        if (target == null || target.trim().isEmpty()) return null;
        String path = target.replace('\\', '/');
        if (path.startsWith("/")) path = path.substring(1);
        else if (!path.startsWith("xl/")) path = "xl/" + path;
        List<String> parts = new ArrayList<>();
        for (String part : path.split("/")) {
            if (part.isEmpty() || ".".equals(part)) continue;
            if ("..".equals(part)) {
                if (parts.isEmpty()) return null;
                parts.remove(parts.size() - 1);
            } else {
                parts.add(part);
            }
        }
        String normalized = join(parts, "/");
        if (!normalized.startsWith("xl/worksheets/") || !normalized.endsWith(".xml")) return null;
        return normalized;
    }

    private static String join(List<String> items, String separator) {
        StringBuilder out = new StringBuilder();
        for (String item : items) {
            if (out.length() > 0) out.append(separator);
            out.append(item);
        }
        return out.toString();
    }

    private static Map<String, byte[]> readOfficeArchive(byte[] bytes, boolean docx) throws IOException {
        Map<String, byte[]> found = new LinkedHashMap<>();
        long totalInflated = 0;
        int entryCount = 0;
        int storedXmlBytes = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zip.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_ARCHIVE_ENTRIES) throw new IOException("В документе слишком много ZIP-записей.");
                String name = entry.getName() == null ? "" : entry.getName().replace('\\', '/');
                boolean unsafeName = name.startsWith("/") || name.contains("../") || name.equals("..") || name.contains("/..");
                boolean candidate = !entry.isDirectory() && !unsafeName && isWantedXmlEntry(name, docx);
                ByteArrayOutputStream collected = candidate ? new ByteArrayOutputStream() : null;
                int entryBytes = 0;
                int read;
                while ((read = zip.read(buffer)) != -1) {
                    entryBytes += read;
                    totalInflated += read;
                    if (entryBytes > MAX_SINGLE_ENTRY_BYTES) throw new IOException("Одна из частей Office-документа превышает лимит 20 МБ.");
                    if (totalInflated > MAX_ARCHIVE_INFLATED_BYTES) throw new IOException("Распакованный Office-документ превышает лимит 30 МБ.");
                    if (candidate && entryBytes > MAX_XML_ENTRY_BYTES) throw new IOException("XML-часть Office-документа превышает лимит 8 МБ.");
                    if (collected != null) collected.write(buffer, 0, read);
                }
                if (collected != null) {
                    storedXmlBytes += entryBytes;
                    if (storedXmlBytes > MAX_STORED_XML_BYTES) throw new IOException("Объём XML-данных документа превышает лимит 15 МБ.");
                    found.put(name, collected.toByteArray());
                }
                zip.closeEntry();
            }
        } catch (IOException error) {
            String message = error.getMessage();
            if (message != null && (message.startsWith("В документе") || message.startsWith("Одна из")
                    || message.startsWith("Распакованный") || message.startsWith("XML-часть")
                    || message.startsWith("Объём XML"))) throw error;
            throw new IOException((docx ? "Не удалось безопасно прочитать DOCX" : "Не удалось безопасно прочитать XLSX")
                + ". Возможно, архив повреждён или защищён неподдерживаемым способом.", error);
        }
        return found;
    }

    private static boolean isWantedXmlEntry(String name, boolean docx) {
        if (docx) return "word/document.xml".equals(name);
        return "xl/workbook.xml".equals(name)
            || "xl/_rels/workbook.xml.rels".equals(name)
            || "xl/sharedStrings.xml".equals(name)
            || (name.startsWith("xl/worksheets/") && name.endsWith(".xml") && !name.contains("/../"));
    }

    private static void parseXml(byte[] xml, DefaultHandler handler, String label) throws IOException {
        if (xml == null || xml.length == 0) throw new IOException("Внутри документа отсутствует XML-данные: " + label + ".");
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            setFactoryFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
            setFactoryFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
            setFactoryFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            setReaderFeature(reader, "http://apache.org/xml/features/disallow-doctype-decl", true);
            setReaderFeature(reader, "http://xml.org/sax/features/external-general-entities", false);
            setReaderFeature(reader, "http://xml.org/sax/features/external-parameter-entities", false);
            reader.setEntityResolver(new EntityResolver() {
                @Override public InputSource resolveEntity(String publicId, String systemId) {
                    return new InputSource(new StringReader(""));
                }
            });
            reader.setErrorHandler(new ErrorHandler() {
                @Override public void warning(SAXParseException error) throws SAXException { throw error; }
                @Override public void error(SAXParseException error) throws SAXException { throw error; }
                @Override public void fatalError(SAXParseException error) throws SAXException { throw error; }
            });
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new ByteArrayInputStream(xml)));
        } catch (SAXException error) {
            String message = error.getMessage();
            if (message != null && message.contains("90 000")) throw new IOException(message, error);
            if (message != null && message.contains("2 000 000")) throw new IOException(message, error);
            throw new IOException("Не удалось безопасно разобрать XML в " + label + ". Документ может быть повреждён или содержать запрещённые XML-конструкции.", error);
        } catch (Exception error) {
            if (error instanceof IOException) throw (IOException) error;
            throw new IOException("Ошибка чтения структуры " + label + ".", error);
        }
    }

    private static void setFactoryFeature(SAXParserFactory factory, String name, boolean value) {
        try { factory.setFeature(name, value); } catch (Exception ignored) { }
    }

    private static void setReaderFeature(XMLReader reader, String name, boolean value) {
        try { reader.setFeature(name, value); } catch (Exception ignored) { }
    }

    private static String localName(String qName) {
        if (qName == null) return "";
        int colon = qName.lastIndexOf(':');
        return colon >= 0 ? qName.substring(colon + 1) : qName;
    }

    private static String attribute(Attributes attributes, String wanted) {
        for (int i = 0; i < attributes.getLength(); i++) {
            String qName = attributes.getQName(i);
            if (wanted.equals(qName) || qName.endsWith(":" + wanted)) return attributes.getValue(i);
        }
        return null;
    }

    private static void appendBounded(StringBuilder out, String value) throws IOException {
        if (value == null || value.isEmpty()) return;
        if (out.length() + value.length() > MAX_EXTRACTED_CHARS) {
            throw new IOException("Извлечённый текст превышает лимит 90 000 символов. Используйте меньший документ или импортируйте только нужные разделы.");
        }
        out.append(value);
    }

    private static void appendSaxBounded(StringBuilder out, String value, int maxChars, String limitMessage) throws SAXException {
        if (value == null || value.isEmpty()) return;
        if (out.length() + value.length() > maxChars) throw new SAXException(limitMessage);
        out.append(value);
    }

    private static String safeLabel(String value) {
        if (value == null || value.trim().isEmpty()) return "Без названия";
        String clean = value.replace('\n', ' ').replace('\r', ' ').replace('\u0000', ' ').trim();
        return clean.length() > 100 ? clean.substring(0, 100) + "…" : clean;
    }

    private static String normalize(String text) {
        if (text == null) return "";
        String clean = text.replace('\u0000', ' ').replace('\f', '\n').replace("\r\n", "\n").replace('\r', '\n');
        clean = clean.replaceAll("[\\t ]+\\n", "\n").replaceAll("\\n{3,}", "\n\n").trim();
        if (clean.length() > MAX_EXTRACTED_CHARS) clean = clean.substring(0, MAX_EXTRACTED_CHARS);
        return clean;
    }

    private static final class DocxHandler extends DefaultHandler {
        final StringBuilder text = new StringBuilder();
        private boolean inText;

        @Override public void startElement(String uri, String local, String qName, Attributes attributes) throws SAXException {
            String name = localName(qName);
            if ("t".equals(name)) inText = true;
            else if ("tab".equals(name)) appendSaxBounded(text, "\t", MAX_EXTRACTED_CHARS, "Извлечённый текст превышает лимит 90 000 символов.");
            else if ("br".equals(name) || "cr".equals(name)) appendSaxBounded(text, "\n", MAX_EXTRACTED_CHARS, "Извлечённый текст превышает лимит 90 000 символов.");
            else if ("tc".equals(name) && text.length() > 0 && text.charAt(text.length() - 1) != '\n') appendSaxBounded(text, " | ", MAX_EXTRACTED_CHARS, "Извлечённый текст превышает лимит 90 000 символов.");
        }

        @Override public void characters(char[] chars, int start, int length) throws SAXException {
            if (inText && length > 0) appendSaxBounded(text, new String(chars, start, length), MAX_EXTRACTED_CHARS,
                "Извлечённый текст превышает лимит 90 000 символов.");
        }

        @Override public void endElement(String uri, String local, String qName) throws SAXException {
            String name = localName(qName);
            if ("t".equals(name)) inText = false;
            else if ("p".equals(name) || "tr".equals(name)) {
                if (text.length() > 0 && text.charAt(text.length() - 1) != '\n') appendSaxBounded(text, "\n", MAX_EXTRACTED_CHARS,
                    "Извлечённый текст превышает лимит 90 000 символов.");
            }
        }
    }

    private static final class SheetRef {
        final String name;
        final String relationshipId;
        final String path;
        SheetRef(String name, String relationshipId, String path) {
            this.name = name == null ? "Лист" : name;
            this.relationshipId = relationshipId == null ? "" : relationshipId;
            this.path = path == null ? "" : path;
        }
    }

    private static final class WorkbookHandler extends DefaultHandler {
        private final List<SheetRef> sheets;
        WorkbookHandler(List<SheetRef> sheets) { this.sheets = sheets; }
        @Override public void startElement(String uri, String local, String qName, Attributes attributes) {
            if (!"sheet".equals(localName(qName))) return;
            String name = attribute(attributes, "name");
            String relation = attribute(attributes, "id");
            if (name != null && relation != null) sheets.add(new SheetRef(name, relation, ""));
        }
    }

    private static final class RelationshipHandler extends DefaultHandler {
        private final Map<String, String> targets;
        RelationshipHandler(Map<String, String> targets) { this.targets = targets; }
        @Override public void startElement(String uri, String local, String qName, Attributes attributes) {
            if (!"Relationship".equals(localName(qName))) return;
            String id = attribute(attributes, "Id");
            String target = attribute(attributes, "Target");
            String type = attribute(attributes, "Type");
            if (id != null && target != null && (type == null || type.endsWith("/worksheet"))) targets.put(id, target);
        }
    }

    private static final class SharedStringsHandler extends DefaultHandler {
        final List<String> values = new ArrayList<>();
        private StringBuilder current;
        private boolean inString;
        private boolean inText;
        private int phoneticDepth;
        private int totalChars;

        @Override public void startElement(String uri, String local, String qName, Attributes attributes) throws SAXException {
            String name = localName(qName);
            if ("si".equals(name)) { inString = true; current = new StringBuilder(); }
            else if ("rPh".equals(name)) phoneticDepth++;
            else if ("t".equals(name) && inString && phoneticDepth == 0) inText = true;
        }

        @Override public void characters(char[] chars, int start, int length) throws SAXException {
            if (inText && current != null && length > 0) {
                totalChars += length;
                if (totalChars > MAX_SHARED_TEXT_CHARS) throw new SAXException("Объём общих строк XLSX превышает лимит 2 000 000 символов.");
                current.append(chars, start, length);
            }
        }

        @Override public void endElement(String uri, String local, String qName) {
            String name = localName(qName);
            if ("t".equals(name)) inText = false;
            else if ("rPh".equals(name) && phoneticDepth > 0) phoneticDepth--;
            else if ("si".equals(name)) {
                values.add(current == null ? "" : current.toString());
                current = null;
                inString = false;
            }
        }
    }

    private static final class SheetHandler extends DefaultHandler {
        final StringBuilder text = new StringBuilder();
        private final List<String> sharedStrings;
        private TreeMap<Integer, String> row;
        private int rowCount;
        private int currentColumn = -1;
        private String cellType = "";
        private StringBuilder value;
        private StringBuilder formula;
        private StringBuilder inlineText;
        private boolean inValue;
        private boolean inFormula;
        private boolean inInline;
        boolean rowsTruncated;
        boolean columnsTruncated;

        SheetHandler(List<String> sharedStrings) { this.sharedStrings = sharedStrings; }

        @Override public void startElement(String uri, String local, String qName, Attributes attributes) {
            String name = localName(qName);
            if ("row".equals(name)) {
                rowCount++;
                row = rowCount <= MAX_XLSX_ROWS_PER_SHEET ? new TreeMap<Integer, String>() : null;
                if (row == null) rowsTruncated = true;
            } else if ("c".equals(name)) {
                currentColumn = columnIndex(attribute(attributes, "r"));
                cellType = attribute(attributes, "t");
                if (cellType == null) cellType = "";
                value = new StringBuilder();
                formula = new StringBuilder();
                inlineText = new StringBuilder();
            } else if ("v".equals(name)) inValue = true;
            else if ("f".equals(name)) inFormula = true;
            else if ("t".equals(name) && "inlineStr".equals(cellType)) inInline = true;
        }

        @Override public void characters(char[] chars, int start, int length) {
            if (length <= 0) return;
            if (inValue && value != null && value.length() < 100_000) value.append(chars, start, Math.min(length, 100_000 - value.length()));
            else if (inFormula && formula != null && formula.length() < 20_000) formula.append(chars, start, Math.min(length, 20_000 - formula.length()));
            else if (inInline && inlineText != null && inlineText.length() < 100_000) inlineText.append(chars, start, Math.min(length, 100_000 - inlineText.length()));
        }

        @Override public void endElement(String uri, String local, String qName) throws SAXException {
            String name = localName(qName);
            if ("v".equals(name)) inValue = false;
            else if ("f".equals(name)) inFormula = false;
            else if ("t".equals(name)) inInline = false;
            else if ("c".equals(name)) {
                if (row != null && currentColumn >= 0) {
                    String cell = resolveCellValue();
                    if (currentColumn >= MAX_XLSX_COLUMNS_PER_ROW) columnsTruncated = true;
                    else if (!cell.isEmpty()) row.put(currentColumn, cell);
                }
                value = formula = inlineText = null;
                currentColumn = -1;
                cellType = "";
            } else if ("row".equals(name)) {
                if (row != null) writeRow();
                row = null;
            }
        }

        private String resolveCellValue() {
            String raw = value == null ? "" : value.toString();
            if ("s".equals(cellType)) {
                try {
                    int index = Integer.parseInt(raw.trim());
                    if (index >= 0 && index < sharedStrings.size()) raw = sharedStrings.get(index);
                    else raw = "[недоступная строка " + index + "]";
                } catch (Exception ignored) { raw = ""; }
            } else if ("inlineStr".equals(cellType)) {
                raw = inlineText == null ? "" : inlineText.toString();
            }
            if (raw.isEmpty() && formula != null && formula.length() > 0) raw = "=" + formula;
            return raw.replace('\u0000', ' ').replace('\r', ' ').replace('\n', ' ');
        }

        private void writeRow() throws SAXException {
            if (row == null || row.isEmpty()) {
                appendSaxBounded(text, "\n", MAX_EXTRACTED_CHARS, "Извлечённый текст превышает лимит 90 000 символов.");
                return;
            }
            int last = Math.min(row.lastKey(), MAX_XLSX_COLUMNS_PER_ROW - 1);
            for (int col = 0; col <= last; col++) {
                if (col > 0) appendSaxBounded(text, "\t", MAX_EXTRACTED_CHARS, "Извлечённый текст превышает лимит 90 000 символов.");
                String cell = row.get(col);
                if (cell != null) appendSaxBounded(text, cell, MAX_EXTRACTED_CHARS, "Извлечённый текст превышает лимит 90 000 символов.");
            }
            appendSaxBounded(text, "\n", MAX_EXTRACTED_CHARS, "Извлечённый текст превышает лимит 90 000 символов.");
        }

        private int columnIndex(String reference) {
            if (reference == null) return -1;
            int index = 0;
            int chars = 0;
            for (int i = 0; i < reference.length(); i++) {
                char c = Character.toUpperCase(reference.charAt(i));
                if (c < 'A' || c > 'Z') break;
                index = index * 26 + (c - 'A' + 1);
                chars++;
                if (chars > 3) return -1;
            }
            return chars == 0 ? -1 : index - 1;
        }
    }
}
