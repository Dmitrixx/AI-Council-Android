# AI Council 2.27.0

**Multi-provider AI council for Android** — independent answers, audits, deep analysis, local servers, source search, document import and report export.

Modern shell: ink surfaces, mint accent, floating navigation and edge-to-edge content on Android 11+. Debug APK is built by GitHub Actions and published to Releases.

> Primary documentation is in Russian: [README_RU.md](README_RU.md). This English file is a high-level overview for international users.

## What it does

AI Council turns a single prompt into a structured deliberation among up to three AI experts (you choose the providers and roles):

| Mode | Behaviour |
|------|-----------|
| ⚡ Quick answer | One expert replies |
| 🤝 Council | Three experts answer independently, then a coordinator synthesises |
| 🛡 Audit | Two experts + critical review of the answer |
| 🔬 Deep analysis | Full multi-round critique and refinement |
| 🤖 Auto | Heuristic routing (RU / UK / EN) based on the prompt and free/paid slots |

Additional capabilities:

- **Streaming** OpenAI-compatible chat completions (OkHttp SSE)
- **Web research** — Brave, Tavily (user keys), Wikipedia, DuckDuckGo Instant Answer with ranked, deduplicated results and optional page extraction
- **Local document import** — TXT / PDF / DOCX / XLSX + hybrid OCR (PDFBox text layer first, then Android `PdfRenderer` + Tesseract/ML Kit only on image pages)
- **Configurable PDF page limit** (5 by default, up to 20) — persisted setting in the import UI
- **Local AI** — Ollama / LM Studio over Wi-Fi (loopback and private LAN policies)
- **Privacy** — encrypted API keys, secret redaction in errors and before send, optional “local only” mode
- **History & projects** in local SQLite, voice draft, themes (system / light / dark), accessibility live regions
- **Local deliberation intelligence** — disagreement map with negation-aware polarity (Russian and Ukrainian), unit and currency clashes, premise forks, ranked cruxes, arithmetic checks (RU/UK/EN, including word arithmetic), and coordinator instructions not to average cruxes: urgency, evidence grade, obligation, step order, necessary vs sufficient, who decides, burden of proof, and swapped cause and effect.

## Supported providers (OpenAI-compatible)

OpenAI, xAI / Grok, DeepSeek, OpenRouter, Groq, Google Gemini (OpenAI-compatible endpoint), Mistral, any custom endpoint (Ollama, LM Studio, …).

## Build & install

### Debug APK (recommended)

1. Open the latest [Release](https://github.com/Dmitrixx/AI-Council-Android/releases)
2. Download `AI-Council-*.apk` and the matching `.sha256`
3. Verify checksum, then install (you may need to allow unknown sources)

### From source

```bash
git clone https://github.com/Dmitrixx/AI-Council-Android.git
cd AI-Council-Android
# Requires JDK 21 + Android SDK 36
./gradlew assembleDebug   # or `gradle assembleDebug`
```

CI workflow (`.github/workflows/android-build.yml`) runs static checks, desktop smoke tests and produces the debug APK on every relevant push.

## Architecture highlights

Most business logic is deliberately kept **Android-independent** and covered by pure-Java smoke tests:

- `CouncilExecutionEngine` — strategy orchestration
- `WebSearchService` + `WebSearchPolicy` + clients — research pipeline
- `ocr/OcrDocumentProcessor` + `OcrInputPolicy` + `OcrModelManager` — hybrid PDF/image OCR
- `ChatCompletionsClient`, `ProviderModelsClient` — network with cancellation, size limits, secret redaction
- `LocalDataStore` — SQLite history/projects/drafts
- `DeliberationIntelligence` — full local disagreement map, arithmetic checks, residual risk detection, evidence density scoring and calibration signals
- `SecretRedactor`, `ProviderErrorFormatter`, `PromptPrivacyPolicy`, `TaskRoutingPolicy`, …

`MainActivity` still owns UI construction and some coordination (this is the largest remaining technical debt).

## Security notes

- No cleartext by default (Network Security Config). Loopback / local hostnames are allowed for on-device / LAN models.
- Private IPv4 LAN addresses may still be blocked by Android’s config; test on a real device.
- OCR models (`rus` / `ukr`, tessdata_fast 4.1.0) are downloaded only from pinned HTTPS URLs and verified by exact size + SHA-512 before install.
- Secrets are never written to the migration database; they stay in encrypted SharedPreferences.

## Roadmap (short)

See [ROADMAP_RU.md](ROADMAP_RU.md) for the detailed Russian plan. High-level next steps:

1. Further split of `MainActivity` into focused controllers / screens
2. Confirmed device testing (phones + tablets, TalkBack, font scale, rotation)
3. Optional on-device GGUF / llama.cpp path (kept last to avoid mixing with network risk)
4. Expandable OCR language catalogue

## Licence

Apache-2.0 — see [LICENSE-APACHE-2.0.txt](LICENSE-APACHE-2.0.txt) and [THIRD_PARTY_NOTICES_RU.md](THIRD_PARTY_NOTICES_RU.md).

---

**Русская документация:** [README_RU.md](README_RU.md) · [CHANGELOG_RU.md](CHANGELOG_RU.md) · [TEST_RESULTS_RU.md](TEST_RESULTS_RU.md)
