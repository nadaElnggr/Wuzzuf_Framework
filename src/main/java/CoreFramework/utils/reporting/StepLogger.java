package CoreFramework.utils.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * StepLogger collects human-readable steps for the current test.
 * Use it in your page methods or tests together with @Step from Allure.
 */
public class StepLogger {

    private static final ThreadLocal<List<String>> steps =
            ThreadLocal.withInitial(ArrayList::new);

    public static void logStep(String stepText) {
        if (stepText == null || stepText.isBlank()) return;
        steps.get().add(stepText.trim());
    }

    public static List<String> getSteps() {
        return Collections.unmodifiableList(steps.get());
    }

    public static void clear() {
        steps.remove();
    }
}
