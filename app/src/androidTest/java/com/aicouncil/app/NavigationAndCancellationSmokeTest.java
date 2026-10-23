package com.aicouncil.app;

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
