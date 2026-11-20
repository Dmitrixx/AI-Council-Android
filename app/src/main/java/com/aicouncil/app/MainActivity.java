package com.aicouncil.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Launch shell. Full multi-model council still needs provider keys and the
 * execution engine; this screen keeps the on-device checks usable.
 */
public final class MainActivity extends Activity {
    /* APP_VERSION = "3.95.0" */

    private EditText taskField;
    private TextView report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int ink = Color.parseColor("#090B10");
        int fg = Color.parseColor("#E7ECE8");
        int muted = Color.parseColor("#9AA3AD");
        int field = Color.parseColor("#171C22");
        int teal = Color.parseColor("#6EE7CD");

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(ink);
        LinearLayout root = CouncilShell.column(this);
        int pad = CouncilShell.dp(this, 16);
        root.setPadding(pad, pad, pad, pad);

        root.addView(CouncilShell.sectionTitle(this, "AI Council", teal));
        root.addView(CouncilShell.label(this,
                "Локальный контур 3.95.0: кадр решения, сценарный стресс-тест, цена ошибки (лишнее действие или упущенный шанс), арифметика и проверка закрытия. Сеть не используется.",
                muted));

        taskField = CouncilShell.field(this, fg, muted, field, true, 6);
        taskField.setHint("Задача, расчёт или решение");
        root.addView(CouncilShell.labeled(this, "Задача", taskField, muted));

        Button run = CouncilShell.primary(this, "Проверить локально", teal);
        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonLp.topMargin = CouncilShell.dp(this, 8);
        run.setLayoutParams(buttonLp);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runLocal();
            }
        });
        root.addView(run);

        report = new TextView(this);
        report.setTextColor(fg);
        report.setTextSize(14f);
        report.setTextIsSelectable(true);
        report.setPadding(0, CouncilShell.dp(this, 14), 0, CouncilShell.dp(this, 24));
        root.addView(report);

        scroll.addView(root);
        setContentView(scroll);
    }

    private void runLocal() {
        String task = taskField.getText() == null ? "" : taskField.getText().toString().trim();
        if (TextUtils.isEmpty(task)) {
            report.setText("Введите задачу.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        appendBlock(sb, DecisionFramePolicy.block(task));
        appendBlock(sb, DeterministicCalculatorPolicy.evaluateExplicitRequest(task));
        appendBlock(sb, BayesianUpdatePolicy.evaluateExplicitRequest(task));
        appendBlock(sb, DecisionArithmeticPolicy.evaluateExplicitRequest(task));
        appendBlock(sb, LikelihoodRatioPolicy.evaluateExplicitRequest(task));
        appendBlock(sb, BaseRateReminderPolicy.evaluateExplicitRequest(task));
        if (sb.length() == 0) {
            sb.append("Явных чисел для локального пересчёта нет. Ниже — проверка того, как выглядел бы закрытый вердикт без аргументов.\n\n");
        }
        String draft = sb.toString();
        appendBlock(sb, DeliberationIntelligence.localCheckFooter(draft + "\n" + task));
        appendBlock(sb, VerdictClosurePolicy.footer(task, draft));
        report.setText(sb.toString().trim());
    }

    private static void appendBlock(StringBuilder sb, String block) {
        if (block == null || block.trim().isEmpty()) return;
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') sb.append("\n\n");
        sb.append(block.trim()).append("\n\n");
    }
}
