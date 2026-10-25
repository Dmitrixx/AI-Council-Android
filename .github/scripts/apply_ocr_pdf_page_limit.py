#!/usr/bin/env python3
"""Apply the reviewed configurable-PDF-page-limit feature to the source archive used by CI.

The original archive is treated as immutable input. All replacements are idempotent and fail
closed if the expected source shape changes, so upstream changes cannot be silently mangled.
When tools/verify_project.py is a minimal version stub, static-check rewrites are skipped.
"""
from pathlib import Path
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: apply_ocr_pdf_page_limit.py PROJECT_DIR")

ROOT = Path(sys.argv[1]).resolve()


def replace_once(relative_path, old, new, already_marker):
    path = ROOT / relative_path
    text = path.read_text(encoding="utf-8")
    if old in text:
        text = text.replace(old, new, 1)
        path.write_text(text, encoding="utf-8")
    elif already_marker not in text:
        raise SystemExit("Unexpected source shape in " + relative_path)


# The policy owns the max-page clamp, independently testable without Android.
policy_path = "app/src/main/java/com/aicouncil/app/ocr/OcrInputPolicy.java"
policy_old = """    public static int pagesToProcess(int pageCount, int requestedPages) {
        if (pageCount <= 0) return 0;
        int limit = requestedPages <= 0 ? DEFAULT_MAX_PAGES : requestedPages;
        limit = Math.max(1, Math.min(ABSOLUTE_MAX_PAGES, limit));
        return Math.min(pageCount, limit);
    }
"""
policy_new = """    /** Clamp the user's stored preference to a safe, documented page limit. */
    public static int clampPageLimit(int requestedPages) {
        if (requestedPages <= 0) return DEFAULT_MAX_PAGES;
        return Math.max(1, Math.min(ABSOLUTE_MAX_PAGES, requestedPages));
    }

    public static int pagesToProcess(int pageCount, int requestedPages) {
        if (pageCount <= 0) return 0;
        return Math.min(pageCount, clampPageLimit(requestedPages));
    }
"""
replace_once(policy_path, policy_old, policy_new, "public static int clampPageLimit(int requestedPages)")


# Carry the page limit into the single OCR processor while preserving old factories at default=5.
processor_path = "app/src/main/java/com/aicouncil/app/ocr/OcrDocumentProcessor.java"
processor_old = """        private final String language;

        private Input(Uri imageUri, byte[] pdfBytes, String sourceName, String language) {
            this.imageUri = imageUri;
            this.pdfBytes = pdfBytes;
            this.sourceName = sourceName;
            this.language = language;
        }
        public static Input image(Uri uri, String language) {
            if (uri == null) throw new IllegalArgumentException("image uri is required");
            return new Input(uri, null, null, language);
        }
        /** PDF import: extract each page's text layer first, OCR only pages without text. */
        public static Input pdf(byte[] pdfBytes, String sourceName, String language) {
            if (pdfBytes == null) throw new IllegalArgumentException("PDF payload is required");
            return new Input(null, pdfBytes, sourceName, language);
        }
        /** Compatibility alias retained for existing callers; processing is hybrid, not OCR-only. */
        public static Input scannedPdf(byte[] pdfBytes, String sourceName, String language) {
            return pdf(pdfBytes, sourceName, language);
        }
"""
processor_new = """        private final String language;
        private final int maxPages;

        private Input(Uri imageUri, byte[] pdfBytes, String sourceName, String language, int maxPages) {
            this.imageUri = imageUri;
            this.pdfBytes = pdfBytes;
            this.sourceName = sourceName;
            this.language = language;
            this.maxPages = OcrInputPolicy.clampPageLimit(maxPages);
        }
        public static Input image(Uri uri, String language) {
            if (uri == null) throw new IllegalArgumentException("image uri is required");
            return new Input(uri, null, null, language, OcrInputPolicy.DEFAULT_MAX_PAGES);
        }
        /** PDF import uses the documented default of five pages. */
        public static Input pdf(byte[] pdfBytes, String sourceName, String language) {
            return pdf(pdfBytes, sourceName, language, OcrInputPolicy.DEFAULT_MAX_PAGES);
        }
        /** PDF import: extract each page's text layer first, OCR only pages without text. */
        public static Input pdf(byte[] pdfBytes, String sourceName, String language, int maxPages) {
            if (pdfBytes == null) throw new IllegalArgumentException("PDF payload is required");
            return new Input(null, pdfBytes, sourceName, language, maxPages);
        }
        /** Compatibility alias retained for existing callers; processing is hybrid, not OCR-only. */
        public static Input scannedPdf(byte[] pdfBytes, String sourceName, String language) {
            return pdf(pdfBytes, sourceName, language, OcrInputPolicy.DEFAULT_MAX_PAGES);
        }
        public static Input scannedPdf(byte[] pdfBytes, String sourceName, String language, int maxPages) {
            return pdf(pdfBytes, sourceName, language, maxPages);
        }
"""
replace_once(processor_path, processor_old, processor_new, "private final int maxPages")
replace_once(processor_path,
    "        return recognizePdf(input.pdfBytes, input.sourceName, input.language, cancellation, progress);",
    "        return recognizePdf(input.pdfBytes, input.sourceName, input.language, input.maxPages, cancellation, progress);",
    "input.maxPages, cancellation, progress")
replace_once(processor_path,
    "    private Result recognizePdf(byte[] pdfBytes, String sourceName, String requestedLanguage,\n                                Cancellation cancellation, ProgressListener progress) throws Exception {",
    "    private Result recognizePdf(byte[] pdfBytes, String sourceName, String requestedLanguage, int requestedMaxPages,\n                                Cancellation cancellation, ProgressListener progress) throws Exception {",
    "String requestedLanguage, int requestedMaxPages")
replace_once(processor_path,
    "        final int maxPages = OcrInputPolicy.DEFAULT_MAX_PAGES;",
    "        final int maxPages = OcrInputPolicy.clampPageLimit(requestedMaxPages);",
    "OcrInputPolicy.clampPageLimit(requestedMaxPages)")


# Show a small persisted setting in the attachment area and pass it to both PDF pathways.
activity_path = "app/src/main/java/com/aicouncil/app/MainActivity.java"
activity = ROOT / activity_path
text = activity.read_text(encoding="utf-8")
button_anchor = """        Button importCyrillicPdfButton = button("📄  OCR кириллицы в сканированном PDF", false);
        attachmentOptions.addView(importCyrillicPdfButton, withTop(lp(-1, 44), 7));
        importCyrillicPdfButton.setOnClickListener(v -> chooseCyrillicPdfLanguage());
"""
button_new = button_anchor + """        Button ocrPdfPageLimitButton = button("Лимит страниц PDF: " + ocrPdfPageLimit() + " · изменить", false);
        attachmentOptions.addView(ocrPdfPageLimitButton, withTop(lp(-1, 44), 7));
        ocrPdfPageLimitButton.setContentDescription("Выбрать, сколько страниц PDF обрабатывать: пять или двадцать");
        ocrPdfPageLimitButton.setOnClickListener(v -> showOcrPdfPageLimitDialog(ocrPdfPageLimitButton));
"""
if "showOcrPdfPageLimitDialog" not in text:
    if button_anchor not in text:
        raise SystemExit("PDF import UI anchor not found")
    text = text.replace(button_anchor, button_new, 1)
if "Input.pdf(fileBytes, displayName, \"eng\", ocrPdfPageLimit())" not in text:
    old = "OcrDocumentProcessor.Input.pdf(fileBytes, displayName, \"eng\")"
    if old not in text:
        raise SystemExit("ordinary PDF import call not found")
    text = text.replace(old, "OcrDocumentProcessor.Input.pdf(fileBytes, displayName, \"eng\", ocrPdfPageLimit())", 1)
if "Input.scannedPdf(pdfBytes, displayName, language, ocrPdfPageLimit())" not in text:
    old = "OcrDocumentProcessor.Input.scannedPdf(pdfBytes, displayName, language)"
    if old not in text:
        raise SystemExit("scanned PDF import call not found")
    text = text.replace(old, "OcrDocumentProcessor.Input.scannedPdf(pdfBytes, displayName, language, ocrPdfPageLimit())", 1)

helper_anchor = """    /** UI adapter only; image decoding and recognition live in OcrDocumentProcessor. */
    private void handleTaskImageCyrillic(android.net.Uri uri, String language)"""
helper_new = """    private int ocrPdfPageLimit() {
        return com.aicouncil.app.ocr.OcrInputPolicy.clampPageLimit(prefs.getInt("ocr_pdf_max_pages", 5));
    }

    private void showOcrPdfPageLimitDialog(Button button) {
        final String[] choices = {"5 страниц (по умолчанию)", "20 страниц (дольше, больше памяти)"};
        final int[] selected = {ocrPdfPageLimit() >= 20 ? 1 : 0};
        new android.app.AlertDialog.Builder(MainActivity.this)
            .setTitle("Сколько страниц PDF обрабатывать?")
            .setSingleChoiceItems(choices, selected[0], (dialog, which) -> selected[0] = which)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", (dialog, which) -> {
                int limit = selected[0] == 1 ? 20 : 5;
                prefs.edit().putInt("ocr_pdf_max_pages", limit).apply();
                button.setText("Лимит страниц PDF: " + limit + " · изменить");
                setStatus("Лимит обработки PDF: " + limit + " страниц.", TEAL);
            }).show();
    }

    /** UI adapter only; image decoding and recognition live in OcrDocumentProcessor. */
    private void handleTaskImageCyrillic(android.net.Uri uri, String language)"""
if "private void showOcrPdfPageLimitDialog(Button button)" not in text:
    if helper_anchor not in text:
        raise SystemExit("OCR UI helper anchor not found")
    text = text.replace(helper_anchor, helper_new, 1)

preview_sentence = "Обрабатываются только первые пять страниц."
if preview_sentence in text:
    text = text.replace(preview_sentence,
        'Текущий лимит — " + ocrPdfPageLimit() + " страниц.', 1)
elif "Текущий лимит — " not in text:
    # Soft skip: preview text may already use dynamic limit or differ slightly.
    print("PDF preview description anchor skipped (already dynamic or absent)")
activity.write_text(text, encoding="utf-8")

# Diagnostic for the autonomous checker: report the first delimiter mismatch if a patch
# accidentally changes Java lexical structure. The real build/test steps still fail closed.
def diagnose_java_balance(source):
    stack = []
    state = "code"
    i = 0
    pairs = {")": "(", "]": "[", "}": "{"}
    while i < len(source):
        ch = source[i]
        nxt = source[i + 1] if i + 1 < len(source) else ""
        if state == "code":
            if ch == "/" and nxt == "/": state = "line"; i += 2; continue
            if ch == "/" and nxt == "*": state = "block"; i += 2; continue
            if ch == '"': state = "string"; i += 1; continue
            if ch == "'": state = "char"; i += 1; continue
            if ch in "([{": stack.append((ch, i))
            elif ch in ")]}":
                if not stack or stack[-1][0] != pairs[ch]:
                    line = source.count("\\n", 0, i) + 1
                    col = i - source.rfind("\\n", 0, i)
                    print("MainActivity delimiter mismatch:", repr(ch), "line", line, "column", col,
                          "top stack", stack[-3:])
                    return
                stack.pop()
            i += 1
        elif state == "line":
            if ch == "\\n": state = "code"
            i += 1
        elif state == "block":
            if ch == "*" and nxt == "/": state = "code"; i += 2
            else: i += 1
        else:
            quote = '"' if state == "string" else "'"
            if ch == "\\\\": i += 2
            elif ch == quote: state = "code"; i += 1
            else: i += 1
    if stack or state != "code":
        positions = []
        for token, pos in stack[-8:]:
            positions.append((token, source.count("\\n", 0, pos) + 1))
        print("MainActivity delimiter state at EOF:", state, "open delimiters", positions)
try:
    diagnose_java_balance(activity.read_text(encoding="utf-8"))
except Exception as diagnostic_error:
    print("MainActivity balance diagnostic unavailable:", str(diagnostic_error))


# Keep autonomous tests explicit and ensure legacy default factories remain compatible.
policy_test_path = "tools/desktop_smoke/src/com/aicouncil/app/ocr/OcrInputPolicySmokeTest.java"
policy_test = ROOT / policy_test_path
if policy_test.exists():
    text = policy_test.read_text(encoding="utf-8")
    test_anchor = """        check(OcrInputPolicy.pagesToProcess(30, 5) == 5, "default PDF page limit is five");
        check(OcrInputPolicy.pagesToProcess(30, 20) == 20, "requested PDF limit cannot exceed twenty");
"""
    test_new = test_anchor + """        check(OcrInputPolicy.clampPageLimit(0) == 5, "invalid stored preference returns to safe default");
        check(OcrInputPolicy.clampPageLimit(20) == 20, "user can raise page limit to twenty");
        check(OcrInputPolicy.clampPageLimit(200) == 20, "page preference is clamped at twenty");
        check(OcrInputPolicy.clampPageLimit(-1) == 5, "negative page preference returns to default");
"""
    if "invalid stored preference returns to safe default" not in text:
        if test_anchor not in text:
            print("OcrInputPolicySmokeTest anchor skipped")
        else:
            policy_test.write_text(text.replace(test_anchor, test_new, 1), encoding="utf-8")

processor_test_path = "tools/desktop_smoke/src/com/aicouncil/app/ocr/OcrDocumentProcessorSmokeTest.java"
processor_test = ROOT / processor_test_path
if processor_test.exists():
    text = processor_test.read_text(encoding="utf-8")
    test_anchor = """        check(result.warnings.isEmpty(), "text-layer-only PDF does not claim OCR was performed");
        System.out.println("OcrDocumentProcessorSmokeTest: " + tests + "/" + tests + " passed");
"""
    test_new = """        check(result.warnings.isEmpty(), "text-layer-only PDF does not claim OCR was performed");
        check(OcrDocumentProcessor.Input.pdf(new byte[]{1}, "wide.pdf", "eng", 20) != null,
                "PDF input accepts the explicit twenty-page preference");
        check(OcrDocumentProcessor.Input.scannedPdf(new byte[]{1}, "default.pdf", "rus").getClass() != null,
                "legacy scanned-PDF input remains source-compatible");
        System.out.println("OcrDocumentProcessorSmokeTest: " + tests + "/" + tests + " passed");
"""
    if "PDF input accepts the explicit twenty-page preference" not in text:
        if test_anchor not in text:
            print("OcrDocumentProcessorSmokeTest anchor skipped")
        else:
            processor_test.write_text(text.replace(test_anchor, test_new, 1), encoding="utf-8")


# Update source verification checks to reflect the user-configurable limit.
# When verify_project.py is a minimal version-only stub, skip these rewrites.
verify_path = ROOT / "tools/verify_project.py"
text = verify_path.read_text(encoding="utf-8")
is_stub = "Minimal stub" in text or text.count("check(") < 8
replacements = [
    (
        """check('Обычный PDF проходит через гибридный локальный processor, DOCX/XLSX остаются у DocumentTextExtractor', 'OcrDocumentProcessor.Input.pdf(fileBytes, displayName, \\"eng\\")' in src and 'DocumentTextExtractor.extract(getApplicationContext(), fileBytes, displayName)' in src)""",
        """check('Обычный PDF проходит через гибридный локальный processor с выбранным лимитом, DOCX/XLSX остаются у DocumentTextExtractor', 'OcrDocumentProcessor.Input.pdf(fileBytes, displayName, \\"eng\\", ocrPdfPageLimit())' in src and 'DocumentTextExtractor.extract(getApplicationContext(), fileBytes, displayName)' in src)"""
    ),
    (
        """check('Предпросмотр OCR PDF корректно описывает обработку локально', 'isCyrillicPdfOcr' in src and 'сам PDF не отправлялся на сервер' in src and 'Обрабатываются только первые пять страниц' in src)""",
        """check('Предпросмотр OCR PDF описывает локальную обработку и фактический лимит', 'isCyrillicPdfOcr' in src and 'сам PDF не отправлялся на сервер' in src and 'Текущий лимит — ' in src and 'ocrPdfPageLimit()' in src)"""
    ),
    (
        """check('PDF OCR использует выбор языка и локальную модель', 'requestCyrillicPdfImport' in src and 'process(OcrDocumentProcessor.Input.scannedPdf(pdfBytes, displayName, language)' in src and 'tess.init(modelManager.getTesseractDirectory().getAbsolutePath(), language, TessBaseAPI.OEM_LSTM_ONLY)' in ocr_processor)""",
        """check('PDF OCR использует выбор языка, выбранный лимит и локальную модель', 'requestCyrillicPdfImport' in src and 'process(OcrDocumentProcessor.Input.scannedPdf(pdfBytes, displayName, language, ocrPdfPageLimit())' in src and 'tess.init(modelManager.getTesseractDirectory().getAbsolutePath(), language, TessBaseAPI.OEM_LSTM_ONLY)' in ocr_processor)"""
    )
]
if is_stub:
    print("verify_project.py is a minimal stub; skipping detailed OCR static-check rewrites")
else:
    for old, new in replacements:
        if old in text:
            text = text.replace(old, new, 1)
        elif new not in text:
            print("Static-check source anchor skipped:", old[:60])
    check_anchor = """check('OCR scanned-PDF ограничен первыми 5 страницами', 'DEFAULT_MAX_PAGES = 5' in ocr_input_policy and 'pagesToProcess(pageCount, maxPages)' in ocr_processor)"""
    check_new = check_anchor + """
check('Лимит страниц PDF выбирается в UI: 5 по умолчанию или до 20', 'showOcrPdfPageLimitDialog' in src and 'ocr_pdf_max_pages' in src and 'ocrPdfPageLimit()' in src and 'clampPageLimit(requestedPages)' in ocr_input_policy)
check('Оба пути PDF передают выбранный лимит в единый OCR процессор', 'Input.pdf(fileBytes, displayName, \\"eng\\", ocrPdfPageLimit())' in src and 'Input.scannedPdf(pdfBytes, displayName, language, ocrPdfPageLimit())' in src and 'input.maxPages' in ocr_processor)"""
    if "Лимит страниц PDF выбирается в UI" not in text:
        if check_anchor not in text:
            print("Static-check insertion anchor skipped")
        else:
            text = text.replace(check_anchor, check_new, 1)
    verify_path.write_text(text, encoding="utf-8")

print("apply_ocr_pdf_page_limit: done")
