package be.example.lib.examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ParameterizedExamplesTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 8, 13})
    void allFibonacciSampleValuesArePositive(int value) {
        assertThat(value).isPositive();
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void blankInputsAreRejected(String input) {
        assertThat(input == null || input.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void blankOrWhitespaceInputsAreRejected(String input) {
        assertThat(input == null || input.isBlank()).isTrue();
    }

    // FastCSV backs @CsvSource since JUnit 6; note "commentCharacter" attribute
    // if your values legitimately contain '#'.
    @ParameterizedTest
    @CsvSource({
        "0, 0, 0",
        "-1, 1, 0",
        "10, -20, -10"
    })
    void addsCsvSourcedInputs(int a, int b, int expected) {
        assertThat(a + b).isEqualTo(expected);
    }

    // Reads src/test/resources/data/sums.csv (header row + one column set per line)
    @ParameterizedTest
    @CsvFileSource(resources = "/data/sums.csv", numLinesToSkip = 1)
    void addsFileSourcedInputs(int a, int b, int expected) {
        assertThat(a + b).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(Weekday.class)
    void everyWeekdayHasAName(Weekday day) {
        assertThat(day.name()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(value = Weekday.class, names = {"SATURDAY", "SUNDAY"})
    void weekendDaysAreFlagged(Weekday day) {
        assertThat(day.isWeekend()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("additionCases")
    void addsMethodSourcedInputs(int a, int b, int expected) {
        assertThat(a + b).isEqualTo(expected);
    }

    static Stream<Arguments> additionCases() {
        return Stream.of(
                arguments(1, 1, 2),
                arguments(2, 3, 5),
                arguments(-5, 5, 0));
    }

    enum Weekday {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

        boolean isWeekend() {
            return this == SATURDAY || this == SUNDAY;
        }
    }
}
