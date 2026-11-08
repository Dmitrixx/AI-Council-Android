package com.aicouncil.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/** Shared programmatic UI widgets for MainActivity (no AndroidX). */
final class CouncilShell {
    private CouncilShell() {}

    static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return l;
    }

    static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, 6);
        lp.bottomMargin = dp(c, 6);
        l.setLayoutParams(lp);
        return l;
    }

    static TextView sectionTitle(Context c, String t, int teal) {
        TextView tv = new TextView(c);
        tv.setText(t);
        tv.setTextSize(16f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(teal);
        tv.setPadding(0, dp(c, 12), 0, dp(c, 6));
        return tv;
    }

    static TextView label(Context c, String t, int muted) {
        TextView tv = new TextView(c);
        tv.setText(t);
        tv.setTextSize(13f);
        tv.setTextColor(muted);
        tv.setPadding(0, 0, dp(c, 8), 0);
        return tv;
    }

    static EditText field(Context c, int text, int muted, int field, boolean multi, int lines) {
        EditText et = new EditText(c);
        et.setSingleLine(!multi);
        if (multi) {
            et.setMinLines(lines);
            et.setMaxLines(Math.max(lines * 3, 12));
            et.setGravity(Gravity.TOP | Gravity.START);
            et.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }
        et.setTextSize(14f);
        et.setTextColor(text);
        et.setHintTextColor(muted);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(c, 8));
        bg.setColor(field);
        et.setBackground(bg);
        et.setPadding(dp(c, 10), dp(c, 8), dp(c, 10), dp(c, 8));
        return et;
    }

    static Spinner spinner(Context c, String[] items) {
        Spinner sp = new Spinner(c);
        sp.setAdapter(new ArrayAdapter<>(c, android.R.layout.simple_spinner_dropdown_item, items));
        return sp;
    }

    static Button primary(Context c, String t, int teal) {
        Button b = new Button(c);
        b.setText(t);
        b.setAllCaps(false);
        b.setTextColor(Color.BLACK);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{teal, Color.rgb(80, 200, 180)});
        bg.setCornerRadius(dp(c, 12));
        b.setBackground(bg);
        return b;
    }

    static Button secondary(Context c, String t, int text, int card) {
        Button b = new Button(c);
        b.setText(t);
        b.setAllCaps(false);
        b.setTextColor(text);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(c, 12));
        bg.setColor(card);
        b.setBackground(bg);
        return b;
    }

    static Button pill(Context c, String t, boolean active, int text, int teal, int card) {
        Button b = new Button(c);
        b.setText(t);
        b.setAllCaps(false);
        b.setTextSize(12f);
        b.setTextColor(active ? Color.BLACK : text);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(c, 20));
        bg.setColor(active ? teal : card);
        b.setBackground(bg);
        return b;
    }

    static LinearLayout.LayoutParams expand(Context c) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(c, 4), 0, dp(c, 4), 0);
        return lp;
    }

    static LinearLayout labeled(Context c, String title, View field, int muted) {
        LinearLayout box = column(c);
        box.addView(label(c, title, muted));
        box.addView(field);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, 4);
        lp.bottomMargin = dp(c, 4);
        box.setLayoutParams(lp);
        return box;
    }
}
