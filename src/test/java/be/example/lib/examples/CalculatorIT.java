package be.example.lib.examples;

import static org.assertj.core.api.Assertions.assertThat;

import be.example.lib.Calculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Naming convention *IT (not *Test) is what tells Failsafe to run this
 * during `mvn verify` rather than the faster `mvn test` unit-test phase.
 * Use this bucket for anything hitting a real DB, filesystem, or network.
 */
@Execution(ExecutionMode.SAME_THREAD) // opt out of parallel execution: shares external state
class CalculatorIT {

    private final Calculator calculator = new Calculator();

    @Test
    void addWorksAgainstA_RealisticWorkload() {
        int result = calculator.add(1_000_000, 2_000_000);
        assertThat(result).isEqualTo(3_000_000);
    }
}
