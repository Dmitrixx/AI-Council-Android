#!/usr/bin/env python3
"""Add Android instrumented navigation/accessibility and cancellation-control smoke tests."""
from pathlib import Path
import sys

if len(sys.argv) != 2:
    raise SystemExit("usage: apply_android_ui_smoke_tests.py PROJECT_DIR")

root = Path(sys.argv[1]).resolve()


def replace_once(relative, old, new, marker):
    path = root / relative
    data = path.read_text(encoding="utf-8")
    if old in data:
        path.write_text(data.replace(old, new, 1), encoding="utf-8")
    elif marker not in data:
        raise SystemExit("Expected source anchor not found in " + relative)


# Configure an AndroidJUnitRunner and Espresso dependencies for real-device/emulator tests.
build_file = root / "app/build.gradle"
build = build_file.read_text(encoding="utf-8")
replace_once(
    "app/build.gradle",
    "    defaultConfig {\n        applicationId 'com.aicouncil.app'",
    "    defaultConfig {\n        testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'\n        applicationId 'com.aicouncil.app'",
    "testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'"
)
build = build_file.read_text(encoding="utf-8")
deps_anchor = "    implementation 'androidx.exifinterface:exifinterface:1.4.2'\n}"
deps_new = """    implementation 'androidx.exifinterface:exifinterface:1.4.2'

    androidTestImplementation 'androidx.test:runner:1.6.2'
    androidTestImplementation 'androidx.test:rules:1.6.1'
    androidTestImplementation 'androidx.test.ext:junit:1.2.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
}"""
if deps_anchor in build:
    build_file.write_text(build.replace(deps_anchor, deps_new, 1), encoding="utf-8")
elif "androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'" not in build:
    raise SystemExit("Could not add Android test dependencies to app/build.gradle")


# Make live task status/progress and the selected bottom-nav destination clear to TalkBack.
activity = "app/src/main/java/com/aicouncil/app/MainActivity.java"
replace_once(
    activity,
    '        status = text("●  Система готова к работе", 12, TEAL, true); status.setPadding(dp(2), dp(13), dp(2), 0); taskCard.addView(status);',
    '        status = text("●  Система готова к работе", 12, TEAL, true); status.setPadding(dp(2), dp(13), dp(2), 0); status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); taskCard.addView(status);',
    "status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE)"
)
replace_once(
    activity,
    '        taskProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); taskProgress.setMax(100); taskProgress.setProgress(0); if (android.os.Build.VERSION.SDK_INT >= 21) taskProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(TEAL)); progressHead.addView(taskProgress, lp(88,5));',
    '        taskProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); taskProgress.setMax(100); taskProgress.setProgress(0); taskProgress.setContentDescription("Прогресс выполнения задачи"); if (android.os.Build.VERSION.SDK_INT >= 21) taskProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(TEAL)); progressHead.addView(taskProgress, lp(88,5));',
    'taskProgress.setContentDescription("Прогресс выполнения задачи")'
)
replace_once(
    activity,
    '            item.setFocusable(true); item.setClickable(true);',
    '            item.setFocusable(true); item.setClickable(true); item.setSelected(selected); item.setContentDescription((selected ? "Выбран раздел: " : "Открыть раздел: ") + navTitles[ni]);',
    'item.setContentDescription((selected ? "Выбран раздел: " : "Открыть раздел: ") + navTitles[ni])'
)
replace_once(
    activity,
    '                    navItems[j].setBackground(active ? shape(TEAL, 17, 0) : shape(Color.TRANSPARENT, 17, 0));',
    '                    navItems[j].setBackground(active ? shape(TEAL, 17, 0) : shape(Color.TRANSPARENT, 17, 0));\n                    navItems[j].setSelected(active);\n                    navItems[j].setContentDescription((active ? "Выбран раздел: " : "Открыть раздел: ") + navTitles[j]);',
    'navItems[j].setContentDescription((active ? "Выбран раздел: " : "Открыть раздел: ") + navTitles[j])'
)

# UI tests exercise the public navigation interactions and cancellation button handler. The
# cancellation test injects a running flag only to avoid sending a live provider request.
test_path = root / "app/src/androidTest/java/com/aicouncil/app/NavigationAndCancellationSmokeTest.java"
test_path.parent.mkdir(parents=True, exist_ok=True)
test_content = r'''package com.aicouncil.app;

import android.view.View;
import android.widget.Button;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class NavigationAndCancellationSmokeTest {
    @Rule
    public final ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void opensAllThreeMainTabsAndUpdatesAccessibleSelection() {
        onView(withContentDescription(anyOf(
                is("Открыть раздел: История"),
                is("Выбран раздел: История")))).perform(click());
        onView(withText("История задач")).check(matches(isDisplayed()));
        onView(withContentDescription("Выбран раздел: История")).check(matches(isDisplayed()));

        onView(withContentDescription(anyOf(
                is("Открыть раздел: Подключения"),
                is("Выбран раздел: Подключения")))).perform(click());
        onView(withText("Подключения ИИ")).check(matches(isDisplayed()));
        onView(withContentDescription("Выбран раздел: Подключения")).check(matches(isDisplayed()));

        onView(withContentDescription(anyOf(
                is("Открыть раздел: Штаб"),
                is("Выбран раздел: Штаб")))).perform(click());
        onView(withText("Ваш ИИ-штаб")).check(matches(isDisplayed()));
        onView(withContentDescription("Выбран раздел: Штаб")).check(matches(isDisplayed()));
    }

    @Test
    public void stopButtonSignalsUserCancellationAndDisablesRepeatTap() throws Exception {
        activityRule.runOnUiThread(() -> {
            try {
                Field working = MainActivity.class.getDeclaredField("working");
                working.setAccessible(true);
                working.setBoolean(activityRule.getActivity(), true);

                Field stopButtonField = MainActivity.class.getDeclaredField("stopButton");
                stopButtonField.setAccessible(true);
                Button stop = (Button) stopButtonField.get(activityRule.getActivity());
                stop.setVisibility(View.VISIBLE);
                stop.setEnabled(true);
                stop.setAlpha(1f);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Cannot prepare cancellation smoke test", e);
            }
        });

        onView(withText("Остановить")).check(matches(isDisplayed())).perform(click());
        onView(withText("■  Остановка запрошена…")).check(matches(isDisplayed()));
        onView(withText("Остановить")).check(matches(not(isEnabled())));
    }
}
'''
if test_path.exists():
    existing = test_path.read_text(encoding="utf-8")
    if "opensAllThreeMainTabsAndUpdatesAccessibleSelection" not in existing:
        raise SystemExit("Unexpected existing instrumentation test")
else:
    test_path.write_text(test_content, encoding="utf-8")

# Ensure the patch can be safely re-run in the workflow without duplicate edits.
print("Added accessible navigation state and Android instrumentation smoke tests.")
