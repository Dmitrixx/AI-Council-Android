# AI Council 2.4.2

**Multi-provider AI council for Android** — independent answers, audits, deep analysis, local servers, source search, document import and report export.

Modern shell: ink surfaces, mint accent, floating navigation and edge-to-edge content on Android 11+. Debug APK is built by GitHub Actions and published to Releases.

> Primary documentation is in Russian:
> [README_RU.md](README_RU.md).

This English file is a high-level overview for international users.

## What it does

AI Council turns a single prompt into a structured deliberation among up to three AI experts (you choose the providers and roles):

| Mode | Behaviour |
| --- | --- |
| ⚡ Quick answer | One expert replies |
| 🤝 Council | Three experts answer independently, then a coordinator synthesises |
| 🛡 Audit | Two experts + critical review of the answer |
| 🔬 Deep analysis | Full multi-round critique and refinement |
| 🤖 Auto | Heuristic routing (RU / UK / EN) based on the prompt and free/paid slots |

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

- `CouncilAnalysisPolicy`, `ExpertRolePolicy`, `TaskRoutingPolicy`, `DeliberationIntelligence`
- Local arithmetic verification, numeric conflict detection, agreement scoring
- Outside view / steelmanning / residual risk prompts baked into expert roles

## Licence

See repository for licence terms.
