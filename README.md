# AI Council 2.40.0

**Multi-provider AI council for Android** — independent answers, audits, deep analysis, local servers, source search, document import and report export.

Modern shell: ink surfaces, mint accent, floating navigation and edge-to-edge content on Android 11+. Version 2.40.0 adds expanded local reasoning-risk signals including appeal to fear, loaded questions, narrative fallacy, just-world, availability and confirmation bias on top of the four-slot council, bounded material import and deterministic calculator. The signals are hypotheses requiring context, not automatic fallacy verdicts.

> Primary documentation is in Russian: [README_RU.md](README_RU.md). This English file is a high-level overview for international users.

## What it does

AI Council turns a single prompt into a multi-stage deliberation using configurable provider slots. Council mode runs three independent experts—including an evidence/falsification reviewer—then a separate coordinator in slot four. Deep analysis adds cyclic critique among the three experts.

| Mode | Behaviour |
|------|-----------|
| ⚡ Quick answer | One expert replies |
| 🤝 Council | Three independent expert answers, then a separate coordinator synthesis |
| 🛡 Audit | Two experts + critical review of the answer |
| 🔬 Deep analysis | Three experts, cyclic peer critique, then coordinator synthesis |
| 🤖 Auto | Heuristic routing (RU / UK / EN) based on the prompt and free/paid slots |

Additional capabilities:

- **Streaming** OpenAI-compatible chat completions (OkHttp SSE)
- **Web research** — Brave, Tavily (user keys), Wikipedia, DuckDuckGo Instant Answer with ranked, deduplicated results and optional page extraction
- **Local material import** — select up to 8 TXT / CSV / source files / PDF / DOCX / XLSX documents or photos in one picker; extracted text and locally OCR-recognized photo text appear in one editable preview before being added to the task. Files are bounded by size/count/text limits and are not sent for extraction.
- **Configurable PDF page limit** (5 by default, up to 20) — persisted setting in the import UI
- **Local AI** — Ollama / LM Studio over Wi-Fi (loopback and private LAN policies)
- **Privacy** — encrypted API keys, secret redaction in errors and before send. “Local-only AI” forcibly disables external web search; ordinary web research sends the search query to the selected external search provider.
- **Local deliberation intelligence** — deterministic arithmetic checks, numeric/unit and currency conflicts, negation/polarity, evidence quality, base-rate/reference-class gaps, uncertainty, causal direction, reversibility, conditions, quantifiers and logical premise clashes. These checks run locally and do not themselves browse the network.
- **Deterministic calculator** — explicit arithmetic requests in Russian, Ukrainian or English are evaluated locally before the council runs. Unsupported syntax, division by zero and out-of-range results are rejected; units and domain meaning are not inferred.
- **Reasoning-risk signals** — conservative offline checks flag narrow patterns resembling appeal to ignorance, bandwagon, post hoc, special pleading, slippery slope, hasty generalization, strawman, ad hominem and circular reasoning. Each is labelled as a possible signal and the coordinator is told to inspect context rather than assume it proves an error.
- **History & projects** in local SQLite, voice draft, themes (system / light / dark), accessibility live regions
- **Local deliberation intelligence** — full local (no-network) disagreement map: arithmetic verification (addition, subtraction, multiplication, division, Russian and English percents), numeric/relative/scale conflicts, polarity (RU+EN), directional antonyms, confidence only when the answer actually states it, residual-risk range mismatches, causation vs correlation, time horizon, year clashes, reversibility gaps, case vs base rate, recommendation-target clashes, missing stakeholders, uncovered sub-questions, Jaccard agreement, hedge/absolute calibration, substance and evidence density (0–100). The coordinator receives a resolution line and is instructed not to average critical signals.

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

- `CouncilExecutionEngine` — strategy orchestration (four configured slots: three independent experts + coordinator)
- `WebSearchService` + `WebSearchPolicy` + clients — research pipeline
- `ocr/OcrDocumentProcessor` + `OcrInputPolicy` + `OcrModelManager` — hybrid PDF/image OCR
- `ChatCompletionsClient`, `ProviderModelsClient` — network with cancellation, size limits, secret redaction
- `LocalDataStore` — SQLite history/projects/drafts
- `DeliberationIntelligence` — local disagreement map, arithmetic checks, residual-risk detection, evidence-density scoring and calibration signals
- `ExpertClaim` + `ExpertClaimJsonParser` — Android-independent typed claim contract with strict, versioned JSON parsing; `StructuredClaimAnalyzer` conservatively compares like-for-like statements and normalizes supported units. The three independent experts request marked JSON, valid metadata is stripped from visible prose, and typed conflicts reach the coordinator; malformed/missing JSON falls back to prose. Persistent claim graphs and expandable conflict UI remain follow-up work.
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


## Current scope and limitations

- The council runs three independent experts (including a falsification/evidence reviewer) before a separate coordinator in slot 4. Deep Analysis adds cyclic peer critique.
- A batch may contain up to eight documents, source files or photos. Text extraction and OCR are local. The app shows a combined editable preview and does not run the task until the user confirms insertion and explicitly starts it.
- Photos currently enter the prompt as OCR text. Original image payloads are not yet forwarded to vision-capable models.
- Persistent structured-claim graphs in history and expandable conflict cards remain planned work.
- CI and emulator UI smoke must pass for the exact commit, followed by physical-device acceptance, before calling this a release.
