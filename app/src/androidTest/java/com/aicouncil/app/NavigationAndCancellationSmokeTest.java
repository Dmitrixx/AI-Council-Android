package com.aicouncil.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * UI smoke tests use the real Activity view tree and click listeners directly.
 * This avoids Espresso's dependency on Window focus, which is unreliable on
 * resource-constrained headless GitHub Actions emulators.
 */
@RunWith(AndroidJUnit4.class)
public class NavigationAndCancellationSmokeTest {
    @Rule
    public final ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void opensAllThreeMainTabsAndUpdatesAccessibleSelection() throws Throwable {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            View decor = activityRule.getActivity().getWindow().getDecorView();

            clickNavigationItem(decor, "История", "История задач");
            clickNavigationItem(decor, "Подключения", "Подключения ИИ");
            clickNavigationItem(decor, "Штаб", "Ваш ИИ-штаб");
        });
    }

    @Test
    public void stopButtonSignalsUserCancellationAndDisablesRepeatTap() throws Throwable {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            MainActivity activity = activityRule.getActivity();
            try {
                Field working = MainActivity.class.getDeclaredField("working");
                working.setAccessible(true);
                working.setBoolean(activity, true);

                Field stopButtonField = MainActivity.class.getDeclaredField("stopButton");
                stopButtonField.setAccessible(true);
                Button stop = (Button) stopButtonField.get(activity);
                stop.setVisibility(View.VISIBLE);
                stop.setEnabled(true);
                stop.setAlpha(1f);

                assertTrue("Stop button click listener did not run", stop.performClick());
                assertFalse("Stop button must be disabled after cancellation", stop.isEnabled());

                Field statusField = MainActivity.class.getDeclaredField("status");
                statusField.setAccessible(true);
                TextView status = (TextView) statusField.get(activity);
                assertEquals("■  Остановка запрошена…", status.getText().toString());
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Cannot prepare cancellation smoke test", e);
            }
        });
    }

    private static void clickNavigationItem(View root, String tab, String expectedTitle) {
        View item = findByContentDescription(root, "Открыть раздел: " + tab);
        if (item == null) {
            item = findByContentDescription(root, "Выбран раздел: " + tab);
        }
        assertNotNull("Navigation item not found: " + tab, item);
        assertTrue("Navigation click listener did not run: " + tab, item.performClick());

        View selected = findByContentDescription(root, "Выбран раздел: " + tab);
        assertNotNull("Selected accessibility state missing: " + tab, selected);
        assertTrue("Selected navigation item is not marked selected: " + tab, selected.isSelected());

        TextView title = findText(root, expectedTitle);
        assertNotNull("Screen title not found: " + expectedTitle, title);
        assertTrue("Target screen is not visible: " + expectedTitle, title.isShown());
    }

    private static View findByContentDescription(View view, String description) {
        if (view.getContentDescription() != null
                && description.contentEquals(view.getContentDescription())) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = findByContentDescription(group.getChildAt(i), description);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static TextView findText(View view, String expected) {
        if (view instanceof TextView
                && expected.contentEquals(((TextView) view).getText())) {
            return (TextView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView found = findText(group.getChildAt(i), expected);
                if (found != null) return found;
            }
        }
        return null;
    }
}
