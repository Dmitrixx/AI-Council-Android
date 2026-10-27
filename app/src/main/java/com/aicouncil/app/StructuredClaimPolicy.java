package com.aicouncil.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Prompt/response boundary for typed claims. The visible answer remains prose;
 * a strict JSON payload is carried in explicit markers and silently falls back
 * to prose when the model violates the contract.
 */
public final class StructuredClaimPolicy {
    public static final String START_MARKER = "<<<AI_COUNCIL_CLAIMS_JSON>>>";
    public static final String END_MARKER = "<<<END_AI_COUNCIL_CLAIMS_JSON>>>";
    private static final int MAX_CONFLICTS_IN_PROMPT = 30;
    private static final int MAX_STATEMENT_CHARS = 220;

    public static final class ParsedResponse {
        public final String answer;
        public final List<ExpertClaim> claims;
        public final boolean structured;
        public final String parseError;

        private ParsedResponse(String answer, List<ExpertClaim> claims, boolean structured, String parseError) {
            this.answer = answer == null ? "" : answer;
            this.claims = Collections.unmodifiableList(new ArrayList<>(claims));
            this.structured = structured;
            this.parseError = parseError == null ? "" : parseError;
        }
    }

    private StructuredClaimPolicy() {}

    public static String addContract(String prompt) {
        String base = prompt == null ? "" : prompt;
        return base + "\n\n" + contractInstruction();
    }

    public static String contractInstruction() {
        return "СТРУКТУРИРОВАННЫЕ УТВЕРЖДЕНИЯ ДЛЯ ЛОКАЛЬНОЙ ПРОВЕРКИ:\n"
                + "Сохрани обычный читаемый ответ для пользователя. В самом конце добавь маркер "
                + START_MARKER + ", затем ровно один JSON-объект, затем маркер " + END_MARKER + ". "
                + "Не используй Markdown fence внутри маркеров. JSON-схема: "
                + "{\"schema_version\":1,\"claims\":[{\"id\":\"локальный ID\","
                + "\"statement\":\"одна проверяемая мысль\",\"numeric_value\":number|null,"
                + "\"unit\":\"единица или пустая строка, если числа нет\","
                + "\"polarity\":\"positive|negative|neutral|uncertain\","
                + "\"time_horizon\":\"горизонт или пустая строка\","
                + "\"confidence\":number|null (0..1),"
                + "\"evidence_class\":\"primary_source|secondary_source|measured_data|calculation|expert_judgment|inference|assumption|unsourced\","
                + "\"stakeholders\":[\"стороны\"],\"reversible\":true|false|null,"
                + "\"source_quote\":\"точная короткая цитата или пустая строка\","
                + "\"source_id\":\"ID реально переданного источника или пустая строка\"}]} . "
                + "Не придумывай source_id и цитаты. У каждого числа укажи единицу; для безразмерного числа используй unitless. "
                + "Если чисел нет — numeric_value=null. Не угадывай пропущенную уверенность или обратимость: ставь null. "
                + "Выводи только атомарные, существенные и проверяемые утверждения (не более 12). "
                + "Если данные не позволяют сформировать утверждения, верни пустой claims-массив. "
                + "Если формат не удаётся соблюсти, не ломай основной ответ — приложение попробует использовать обычный текст.";
    }

    public static ParsedResponse parseResponse(String response) {
        String raw = response == null ? "" : response;
        int start = raw.indexOf(START_MARKER);
        int end = raw.indexOf(END_MARKER);
        if (start < 0 || end < 0 || end < start
                || raw.indexOf(START_MARKER, start + START_MARKER.length()) >= 0
                || raw.indexOf(END_MARKER, end + END_MARKER.length()) >= 0
                || !raw.substring(end + END_MARKER.length()).trim().isEmpty()) {
            return fallback(raw, "structured markers missing, duplicated, or not at end");
        }
        String json = raw.substring(start + START_MARKER.length(), end).trim();
        if (json.isEmpty()) return fallback(raw, "structured JSON block is empty");
        try {
            List<ExpertClaim> claims = ExpertClaimJsonParser.parse(json);
            return new ParsedResponse(raw.substring(0, start).trim(), claims, true, "");
        } catch (IllegalArgumentException error) {
            return fallback(raw, error.getMessage());
        }
    }

    public static String formatConflictsForCoordinator(List<ExpertClaim> expertA, List<ExpertClaim> expertB) {
        List<StructuredClaimAnalyzer.Conflict> conflicts = StructuredClaimAnalyzer.compare(expertA, expertB);
        if (conflicts.isEmpty()) return "";
        StringBuilder out = new StringBuilder("СТРУКТУРИРОВАННЫЕ КОНФЛИКТЫ (локальный анализ утверждений; не готовый вывод):\n");
        int count = Math.min(MAX_CONFLICTS_IN_PROMPT, conflicts.size());
        for (int i = 0; i < count; i++) {
            StructuredClaimAnalyzer.Conflict conflict = conflicts.get(i);
            out.append(i + 1).append(". [").append(conflict.type.name()).append("] ")
                    .append(truncate(conflict.statement, MAX_STATEMENT_CHARS)).append("\n")
                    .append("   Эксперт A: ").append(truncate(conflict.expertAValue, 120))
                    .append(sourceSuffix(conflict.sourceA, conflict.quoteA)).append("\n")
                    .append("   Эксперт B: ").append(truncate(conflict.expertBValue, 120))
                    .append(sourceSuffix(conflict.sourceB, conflict.quoteB)).append("\n")
                    .append("   Проверка: ").append(conflict.note).append("\n");
        }
        if (conflicts.size() > count) out.append("Ещё конфликтов: ").append(conflicts.size() - count).append(".\n");
        out.append("Не усредняй значения с несовместимыми единицами. Не считай расхождение доказанной ошибкой без проверки. ")
                .append("Сохрани неразрешённые конфликты и укажи, какой источник или эксперимент мог бы их разрешить.");
        return out.toString();
    }

    private static ParsedResponse fallback(String raw, String error) {
        return new ParsedResponse(raw.trim(), Collections.<ExpertClaim>emptyList(), false, error);
    }

    private static String sourceSuffix(String sourceId, String quote) {
        StringBuilder suffix = new StringBuilder();
        if (sourceId != null && !sourceId.isEmpty()) suffix.append(" [source_id=").append(truncate(sourceId, 80)).append("]");
        if (quote != null && !quote.isEmpty()) suffix.append(" quote=\"").append(truncate(quote, 100)).append("\"");
        return suffix.toString();
    }

    private static String truncate(String value, int maxChars) {
        if (value == null) return "";
        String cleaned = value.replace("\n", " ").replace("\r", " ").trim();
        return cleaned.length() <= maxChars ? cleaned : cleaned.substring(0, maxChars - 1) + "…";
    }
}
