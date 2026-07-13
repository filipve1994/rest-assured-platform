package be.example.lib.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.ClassOrderer;

/**
 * Covers: lifecycle callbacks, per-class vs per-method test instances,
 * @Nested classes with deterministic ordering (new default in JUnit 6),
 * explicit @Order, and @DisplayName.
 */
@DisplayName("Lifecycle & nesting")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // one instance for the whole class -> can use non-static @BeforeAll
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LifecycleAndNestingTest {

    private final List<String> events = new ArrayList<>();

    @BeforeAll
    void initOnce() {
        // Only allowed non-static because of PER_CLASS lifecycle above.
        events.add("beforeAll");
    }

    @BeforeEach
    void initEach() {
        events.add("beforeEach");
    }

    @AfterEach
    void tearDownEach() {
        events.add("afterEach");
    }

    @AfterAll
    void tearDownOnce() {
        assertThat(events).contains("beforeAll", "beforeEach", "afterEach");
    }

    @Test
    @Order(1)
    @DisplayName("runs first because of explicit @Order")
    void runsFirst() {
        assertThat(events).endsWith("beforeEach");
    }

    @Test
    @Order(2)
    void runsSecond() {
        assertThat(events).contains("beforeEach");
    }

    /**
     * JUnit 6 makes @Nested execution order deterministic by default, and
     * @TestMethodOrder / @TestClassOrder on the enclosing class is now
     * inherited by nested classes automatically.
     */
    @Nested
    @DisplayName("when the calculator has been reset")
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)
    class WhenReset {

        @Test
        void startsAtZero() {
            assertThat(0).isZero();
        }

        @Nested
        @DisplayName("and a value is added")
        class AndValueAdded {

            @Test
            void reflectsTheAddedValue() {
                assertThat(1 + 1).isEqualTo(2);
            }
        }
    }
}
