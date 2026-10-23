# Improvements applied in 2.2.0

## Shipped

- versionName **2.2.0** / versionCode **49**
- English `README.md` for international discoverability
- Documented that PDF page-limit UI (5/20) is in source
- CI publishes `AI-Council-2.2.0-debug.apk` to release `v2.2.0`
- Static verifier and release notes updated

## Already present (confirmed)

- Hybrid PDF OCR with selectable page limit
- OkHttp on all major network paths + cancellation
- Hundreds of pure-Java policy/smoke tests
- GitHub Actions debug APK pipeline

## Next engineering priorities

1. Split `MainActivity` into controllers (providers, history, results)
2. Device acceptance matrix (TalkBack, font scale, rotation)
3. Move large root ZIP out of default clone path
4. On-device GGUF/llama.cpp last
