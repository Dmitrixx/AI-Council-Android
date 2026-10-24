# Improvements applied in 2.2.x

## Shipped (recent autonomous)

- **Smarter auto-routing** (`TaskRoutingPolicy`): better detection of planning, research, multi-faceted and high-stakes tasks → more often Council / Deep analysis when experts are available.
- **Stronger coordinator & critique** (`CouncilAnalysisPolicy`): explicit confidence calibration (0–100%), evidence grading (fact / inference / assumption / unverified), bias awareness, counterfactuals, self-revision in peer review.
- **Improved default roles & system prompts** (`ExpertRolePolicy`): step-by-step reasoning first, explicit assumptions and uncertainty, tighter anti-hallucination rules.
- CI continues to apply synthesis patches and publish debug APKs.

## Next priorities

1. Split MainActivity (providers / history / results controllers)
2. Device matrix (TalkBack, font scale, rotation)
3. Keep large root ZIP out of default clone path
4. On-device GGUF last
5. Further refine fallback recovery and tool-use hints based on real runs
