package com.aicouncil.app;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small deterministic arithmetic tool for explicitly requested, unitless calculations.
 * It never evaluates arbitrary code or silently interprets ordinary prose as an expression.
 */
public final class DeterministicCalculatorPolicy {
    private static final int MAX_EXPRESSION_CHARS = 160;
    private static final int MAX_OPERATIONS = 100;
    private static final double MAX_ABS_VALUE = 1.0e12;
    private static final Pattern EXPLICIT_CUE = Pattern.compile(
            "(?iu)(?:посчитай(?:те)?|вычисли(?:те)?|обчисли(?:ть)?|порахуй(?:те)?|calculate|evaluate)\\s*[:：]?\\s*(.+)$");
    private static final Pattern EXPRESSION = Pattern.compile("[0-9eE+*/().\\s-]+");

    private DeterministicCalculatorPolicy() {}

    /** Returns an app-calculated result only when the task contains an explicit calculation cue. */
    public static String evaluateExplicitRequest(String task) {
        if (task == null || task.trim().isEmpty()) return "";
        Matcher cue = EXPLICIT_CUE.matcher(task.trim());
        if (!cue.find()) return "";
        String expression = cue.group(1).trim();
        while (!expression.isEmpty() && "?!.,;".indexOf(expression.charAt(expression.length() - 1)) >= 0) {
            expression = expression.substring(0, expression.length() - 1).trim();
        }
        if (expression.endsWith("=")) expression = expression.substring(0, expression.length() - 1).trim();
        if (expression.length() == 0 || expression.length() > MAX_EXPRESSION_CHARS
                || !EXPRESSION.matcher(expression).matches()
                || !containsOperator(expression)) return "";
        try {
            Parser parser = new Parser(expression);
            double value = parser.parse();
            if (!Double.isFinite(value) || Math.abs(value) > MAX_ABS_VALUE) return "";
            return "ДЕТЕРМИНИРОВАННЫЙ РАСЧЁТ ПРИЛОЖЕНИЯ (не ответ ИИ):\n"
                    + "Выражение: " + expression + "\n"
                    + "Результат: " + String.format(Locale.ROOT, "%.12g", value) + "\n"
                    + "Метод: локальный арифметический парсер; единицы измерения и смысл входных данных не проверялись.";
        } catch (IllegalArgumentException invalid) {
            return "";
        }
    }

    /** Evaluates a raw arithmetic expression; throws when syntax or safety limits fail. */
    public static double evaluateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty() || expression.length() > MAX_EXPRESSION_CHARS
                || !EXPRESSION.matcher(expression).matches()) {
            throw new IllegalArgumentException("unsupported expression");
        }
        Parser parser = new Parser(expression);
        double value = parser.parse();
        if (!Double.isFinite(value) || Math.abs(value) > MAX_ABS_VALUE) {
            throw new IllegalArgumentException("result outside supported range");
        }
        return value;
    }

    private static boolean containsOperator(String expression) {
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '+' || c == '*' || c == '/' || c == '-') return true;
        }
        return false;
    }

    private static final class Parser {
        private final String input;
        private int position;
        private int operations;

        Parser(String input) { this.input = input; }

        double parse() {
            double value = expression();
            skipSpaces();
            if (position != input.length()) throw error();
            return checked(value);
        }

        private double expression() {
            double value = term();
            while (true) {
                skipSpaces();
                if (take('+')) { countOperation(); value = checked(value + term()); }
                else if (take('-')) { countOperation(); value = checked(value - term()); }
                else return value;
            }
        }

        private double term() {
            double value = unary();
            while (true) {
                skipSpaces();
                if (take('*')) { countOperation(); value = checked(value * unary()); }
                else if (take('/')) {
                    countOperation();
                    double divisor = unary();
                    if (divisor == 0.0) throw new IllegalArgumentException("division by zero");
                    value = checked(value / divisor);
                } else return value;
            }
        }

        private double unary() {
            skipSpaces();
            if (take('+')) { countOperation(); return unary(); }
            if (take('-')) { countOperation(); return checked(-unary()); }
            return primary();
        }

        private double primary() {
            skipSpaces();
            if (take('(')) {
                double value = expression();
                skipSpaces();
                if (!take(')')) throw error();
                return value;
            }
            int start = position;
            boolean digits = false;
            while (position < input.length() && Character.isDigit(input.charAt(position))) {
                position++; digits = true;
            }
            if (position < input.length() && input.charAt(position) == '.') {
                position++;
                while (position < input.length() && Character.isDigit(input.charAt(position))) {
                    position++; digits = true;
                }
            }
            if (!digits) throw error();
            if (position < input.length() && (input.charAt(position) == 'e' || input.charAt(position) == 'E')) {
                position++;
                if (position < input.length() && (input.charAt(position) == '+' || input.charAt(position) == '-')) position++;
                int exponentStart = position;
                while (position < input.length() && Character.isDigit(input.charAt(position))) position++;
                if (exponentStart == position) throw error();
            }
            try { return checked(Double.parseDouble(input.substring(start, position))); }
            catch (NumberFormatException invalid) { throw error(); }
        }

        private boolean take(char expected) {
            if (position < input.length() && input.charAt(position) == expected) { position++; return true; }
            return false;
        }

        private void skipSpaces() {
            while (position < input.length() && Character.isWhitespace(input.charAt(position))) position++;
        }

        private void countOperation() {
            if (++operations > MAX_OPERATIONS) throw new IllegalArgumentException("too many operations");
        }

        private double checked(double value) {
            if (!Double.isFinite(value) || Math.abs(value) > MAX_ABS_VALUE) {
                throw new IllegalArgumentException("intermediate result outside supported range");
            }
            return value;
        }

        private IllegalArgumentException error() {
            return new IllegalArgumentException("invalid expression at position " + position);
        }
    }
}
