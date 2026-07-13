package be.example.lib.examples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import be.example.lib.Calculator;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Shows composing a custom extension with Mockito's own extension, and
 * @Mock-based collaborator stubbing/verification.
 */
@ExtendWith({TimingExtension.class, MockitoExtension.class})
class ExtensionsAndMockingExampleTest {

    @Mock
    Supplier<List<Integer>> numberSupplier;

    private final Calculator calculator = new Calculator();

    @Test
    void sumsWhateverTheCollaboratorReturns() {
        when(numberSupplier.get()).thenReturn(List.of(1, 2, 3));

        int total = numberSupplier.get().stream()
                .reduce(0, calculator::add);

        assertThat(total).isEqualTo(6);
        verify(numberSupplier).get();
    }
}
