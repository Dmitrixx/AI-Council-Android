# Improvements applied in 2.2.x / 2.13.x

## Shipped (recent autonomous)

- **2.13.0** ExpertRolePolicy: explicit guards against base-rate neglect, planning fallacy, and overconfidence on rare events; confidence anchored to reference class.
- **Smarter auto-routing** (`TaskRoutingPolicy`): better detection of planning, research, multi-faceted and high-stakes tasks → more often Council / Deep analysis when experts are available.
- **Stronger coordinator & critique** (`CouncilAnalysisPolicy`): explicit confidence calibration (0–100%), evidence grading (fact / inference / assumption / unverified), bias awareness, counterfactuals, self-revision in peer review.
- **Improved default roles & system prompts** (`ExpertRolePolicy`): step-by-step reasoning first, explicit assumptions and uncertainty, tighter anti-hallucination rules.
- CI continues to apply synthesis patches and publish debug APKs.

## Next priorities (autonomous)

1. Ship directional antonym + confidence-mismatch signals in `DeliberationIntelligence` (prepared locally; next commit).
2. Split MainActivity (providers / history / results controllers)
3. Device matrix (TalkBack, font scale, rotation)
4. Keep large root ZIP out of default clone path
5. On-device GGUF last
6. Further refine fallback recovery and tool-use hints based on real runs
