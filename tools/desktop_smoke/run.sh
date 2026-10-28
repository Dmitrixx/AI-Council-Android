#!/usr/bin/env sh
set -eu
SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT INT TERM
find "$SCRIPT_DIR/src" -name '*.java' -print > "$TMP_DIR/sources.txt"
printf '%s\n' \
  "$ROOT/app/src/main/java/com/aicouncil/app/DocumentTextExtractor.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/EndpointPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/WebSearchPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/WebSearchEndpointPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/WebPageFetchPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/LocalAiPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ProviderConfigFingerprint.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ProviderDiagnosticsPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ResultReportFormatter.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ResultExportPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/HistoryProjectPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/StreamingResultPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/SettingsPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/CouncilLaunchPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/FollowUpDialoguePolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/InputImportPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/BackupExportPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/CouncilAnalysisPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/CouncilExecutionEngine.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ExpertClaim.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ExpertClaimJsonParser.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/StructuredClaimAnalyzer.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/StructuredClaimPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/MaterialImportPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/DeterministicCalculatorPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ReasoningFallacyPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/DecisionBriefPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ExpertRolePolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/TruncatedAnswerPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/UiThemePolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ProviderErrorFormatter.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/SecretRedactor.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/PromptPrivacyPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/VoicePromptPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/PromptMetricsPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/TaskRoutingPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/DeliberationIntelligence.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/DeliberationSignals.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ClaimGraph.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/FallbackProviderPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ExpertStatusPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/CouncilResultModel.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/OcrModelIntegrityPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ocr/OcrInputPolicy.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ocr/OcrDocumentProcessor.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/OcrModelManager.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/ProviderModelsClient.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/WebSearchClient.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/WebPageFetchClient.java" \
  "$ROOT/app/src/main/java/com/aicouncil/app/WebSearchService.java" >> "$TMP_DIR/sources.txt"
mkdir -p "$TMP_DIR/out"
javac --release 8 -encoding UTF-8 -d "$TMP_DIR/out" @"$TMP_DIR/sources.txt"
java -cp "$TMP_DIR/out" com.aicouncil.app.ExtractorSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.EndpointPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.WebSearchPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.WebSearchEndpointPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.WebPageFetchPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.LocalAiPolicySmokeTest

java -cp "$TMP_DIR/out" com.aicouncil.app.ProviderConfigFingerprintSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ProviderDiagnosticsPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ResultReportFormatterSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ResultExportPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.HistoryProjectPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.StreamingResultPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.SettingsPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.CouncilLaunchPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.FollowUpDialoguePolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.InputImportPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.BackupExportPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.CouncilAnalysisPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.CouncilExecutionEngineSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ExpertClaimSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ExpertClaimJsonParserSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.StructuredClaimAnalyzerSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.StructuredClaimPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.MaterialImportPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.DeterministicCalculatorPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ReasoningFallacyPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.DecisionBriefPolicySmokeTest

java -cp "$TMP_DIR/out" com.aicouncil.app.ExpertRolePolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.TruncatedAnswerPolicySmokeTest

java -cp "$TMP_DIR/out" com.aicouncil.app.UiThemePolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ProviderErrorFormatterSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.SecretRedactorSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.PromptPrivacyPolicySmokeTest

java -cp "$TMP_DIR/out" com.aicouncil.app.VoicePromptPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.PromptMetricsPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.TaskRoutingPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.DeliberationIntelligenceSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ClaimGraphSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.FallbackProviderPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ExpertStatusPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.TaskRoutingPolicyExtraSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.CouncilResultModelSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.OcrModelIntegrityPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ocr.OcrInputPolicySmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ocr.OcrDocumentProcessorSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.OcrModelManagerSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.ProviderModelsClientSmokeTest
java -cp "$TMP_DIR/out" com.aicouncil.app.WebSearchServiceSmokeTest
