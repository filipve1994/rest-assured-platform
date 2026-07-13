package be.example.lib.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @ParameterizedClass parameterizes every @Test in the class with the same
 * arguments, instead of repeating a @ParameterizedTest source per method.
 * Useful when several assertions must all run against each input row.
 */
@ParameterizedClass
@CsvSource({
    "2, 4",
    "3, 9",
    "5, 25"
})
class ParameterizedClassExampleTest {

    @Parameter(0)
    int base;

    @Parameter(1)
    int expectedSquare;

    @Test
    void squareMatchesExpectation() {
        assertThat(base * base).isEqualTo(expectedSquare);
    }

    @Test
    void squareIsNeverNegative() {
        assertThat(base * base).isNotNegative();
    }
}
