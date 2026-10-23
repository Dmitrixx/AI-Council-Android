# Improvements planned / applied for 2.2.0

## Already present in source (confirmed 2026-10-23)

- Hybrid PDF OCR with user-selectable page limit 5 / 20 (`ocrPdfPageLimit()`, `showOcrPdfPageLimitDialog`, `OcrInputPolicy.clampPageLimit`)
- All major network paths on dedicated OkHttp clients with cancellation
- Policy classes + hundreds of desktop smoke tests
- GitHub Actions debug APK pipeline

## Applied in this branch

- English `README.md` for discoverability and non-Russian contributors
- Clearer high-level overview of architecture and security posture
- Explicit note that the PDF page-limit UI is already in the main tree (no longer only a CI patch)

## Recommended next engineering steps

1. **MainActivity reduction**  
   Extract:  
   - Provider settings panel → `ProviderSettingsController`  
   - History / projects lists → dedicated adapters + small presenters  
   - Result card / streaming UI → `ResultCardsController`  
   Keep pure policy classes untouched; add regression smoke tests for any moved logic.

2. **CI hardening**  
   - Make the “apply_ocr_pdf_page_limit.py” and “apply_android_ui_smoke_tests.py” scripts no-ops when the changes are already present (they already try to be idempotent).  
   - Consider removing the large root ZIP from the default clone path (or move it to Releases only) to speed up `git clone`.

3. **Device acceptance matrix**  
   - Android 8 / 11 / 14 phones + one tablet  
   - TalkBack, 200 % font scale, landscape, theme switch mid-stream  
   - Real Brave / Tavily keys and a local Ollama instance

4. **On-device model (last)**  
   llama.cpp / GGUF only after the network + OCR surface is stable and MainActivity is under ~1500 lines.

## Version bump suggestion

When the next concrete code change lands:

- `versionName` → `2.2.0`
- `versionCode` → `49`
- Update `RELEASE_NOTES_RU.md` and `CHANGELOG_RU.md`
