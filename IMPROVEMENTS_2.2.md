# Improvements applied in 2.2.x / 2.24.x

- **2.25.0** Conditional versus categorical recommendations, universal versus partial quantifiers, and percent versus percentage-point clashes are first-class cruxes. The coordinator is told not to flatten them.
- **2.24.0** Structured claim graph (strict JSON v1 + prose fallback) is fed to the coordinator. Exclusivity, calibration, missing alternatives, extra stakeholders and Russian word arithmetic are now in source, not only the changelog.
- **2.23.0** Exclusivity (forbid vs allow), confidence–evidence calibration, missing alternatives gap, expanded stakeholders/premises, Russian word arithmetic (плюс/минус/…).
- **2.22.0** Negation-aware polarity (RU+UK), km/miles and currency clashes, premise forks, crux ranking, steelman and residual-risk quality gaps, Ukrainian `від` arithmetic.
- **2.20.0** Deliberation map: recommendation-target clash, missing stakeholders, case vs base rate, confidence only next to an explicit uncertainty word, resolution line for the coordinator, English `% of` and `×` arithmetic. CI no longer downgrades versionName to 2.14.0. UI version constant matches Gradle.
