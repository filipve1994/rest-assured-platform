package be.example.lib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculatorTest {

    private final Calculator calculator = new Calculator();

    @Test
    void addsTwoPositiveNumbers() {
        assertThat(calculator.add(2, 3)).isEqualTo(5);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0, 0",
        "-1, 1, 0",
        "10, -20, -10"
    })
    void addsVariousInputs(int a, int b, int expected) {
        assertThat(calculator.add(a, b)).isEqualTo(expected);
    }
}
