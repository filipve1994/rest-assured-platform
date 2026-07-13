package be.example.lib.examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

class ExceptionsTimeoutsAndAssumptionsTest {

    @Test
    void divisionByZeroThrows() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> {
            int unused = 1 / 0;
        });
        assertThat(ex).hasMessageContaining("/ by zero");
    }

    @Test
    void completesWithinDeadline() {
        // Runs on the calling thread; use for I/O-bound or non-interruptible code.
        assertTimeout(Duration.ofMillis(200), () -> Thread.sleep(10));
    }

    @Test
    void abortsIfDeadlineExceeded() {
        // Runs the supplier on a separate thread and aborts if it overruns;
        // note this does NOT stop the original thread's work, only the assertion.
        assertTimeoutPreemptively(Duration.ofMillis(200), () -> "fast enough");
    }

    @Test
    void onlyRunsWhenPreconditionHolds() {
        assumeTrue(System.getProperty("java.version") != null, "JVM must report a version");
        // Body only executes if the assumption passes; otherwise the test is
        // marked "aborted", not failed.
        assertThat(Runtime.version().feature()).isGreaterThanOrEqualTo(21);
    }

    @Test
    @EnabledOnJre({JRE.JAVA_21, JRE.JAVA_25})
    void onlyRunsOnSelectedJreVersions() {
        assertThat(true).isTrue();
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void skippedOnWindowsForPathSeparatorReasons() {
        assertThat(System.getProperty("os.name")).isNotBlank();
    }

    @RepeatedTest(3)
    void flakyLookingLogicIsExercisedMultipleTimes(RepetitionInfo info) {
        assertThat(info.getCurrentRepetition()).isBetween(1, info.getTotalRepetitions());
    }
}
