package be.example.lib.examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class DynamicTestsAndTagsTest {

    /**
     * @TestFactory generates test cases at runtime from data you don't know
     * about at compile time (e.g. loaded from a file, database, or API).
     * Prefer @ParameterizedTest when the shape of the data is fixed and known.
     */
    @TestFactory
    Stream<DynamicTest> generatesOneCheckPerWord() {
        List<String> words = List.of("alpha", "beta", "gamma");
        return words.stream()
                .map(word -> dynamicTest(
                        "\"" + word + "\" is lower case",
                        () -> assertThat(word).isEqualTo(word.toLowerCase())));
    }

    @Test
    @Tag("slow")
    void anExpensiveIntegrationLikeCheck() {
        // Run only tagged subsets via:
        //   mvn test -Dgroups=slow
        //   mvn test -DexcludedGroups=slow
        assertThat(1).isEqualTo(1);
    }

    @Test
    @Tag("fast")
    void aCheapUnitCheck() {
        assertThat(1).isEqualTo(1);
    }
}
