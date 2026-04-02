package com.splendor.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link InputResolver}.
 * Uses ByteArrayInputStream to simulate user input via Scanner and
 * ByteArrayOutputStream to capture prompt output for verification.
 *
 * <p>Each test creates a fresh InputResolver with its own InputStream so that
 * test isolation is guaranteed. System.in and System.out are restored after
 * every test.
 */
@DisplayName("InputResolver Tests")
class InputResolverTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void setUp() {
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    // ── promptForInt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("promptForInt returns valid integer within range")
    void promptForIntValidInput() {
        InputResolver resolver = resolverWithInput("5\n");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(5, result);
    }

    @Test
    @DisplayName("promptForInt accepts lower boundary")
    void promptForIntLowerBound() {
        InputResolver resolver = resolverWithInput("1\n");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(1, result);
    }

    @Test
    @DisplayName("promptForInt accepts upper boundary")
    void promptForIntUpperBound() {
        InputResolver resolver = resolverWithInput("10\n");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(10, result);
    }

    @Test
    @DisplayName("promptForInt returns -1 for 'Z' undo signal")
    void promptForIntUndoZ() {
        InputResolver resolver = resolverWithInput("Z\n");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("promptForInt returns -1 for 'UNDO' signal (case-insensitive)")
    void promptForIntUndoWord() {
        InputResolver resolver = resolverWithInput("undo\n");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("promptForInt re-prompts on non-numeric input then accepts valid")
    void promptForIntNonNumericThenValid() {
        InputResolver resolver = resolverWithInput("abc\n3\n");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(3, result);

        String output = capturedOut.toString();
        assertTrue(output.contains("Invalid number format"),
                "Should print error for non-numeric input");
    }

    @Test
    @DisplayName("promptForInt re-prompts on out-of-range then accepts valid")
    void promptForIntOutOfRangeThenValid() {
        InputResolver resolver = resolverWithInput("99\n5\n");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(5, result);

        String output = capturedOut.toString();
        assertTrue(output.contains("Value must be between"),
                "Should print range error message");
    }

    @Test
    @DisplayName("promptForInt re-prompts on empty input then accepts valid")
    void promptForIntEmptyThenValid() {
        InputResolver resolver = resolverWithInput("\n7\n");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(7, result);

        String output = capturedOut.toString();
        assertTrue(output.contains("Input cannot be empty"),
                "Should print empty input error");
    }

    @Test
    @DisplayName("promptForInt returns -1 on exhausted input (NoSuchElementException)")
    void promptForIntExhaustedInput() {
        // Empty stream → Scanner.nextLine() throws NoSuchElementException → returns -1
        InputResolver resolver = resolverWithInput("");
        int result = resolver.promptForInt("Pick: ", 1, 10);
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("promptForInt invokes onInvalid callback on bad input")
    void promptForIntOnInvalidCallback() {
        InputResolver resolver = resolverWithInput("bad\n5\n");
        final int[] callbackCount = {0};
        int result = resolver.promptForInt("Pick: ", 1, 10, () -> callbackCount[0]++);
        assertEquals(5, result);
        assertEquals(1, callbackCount[0], "onInvalid should have been called once");
    }

    // ── promptForString ─────────────────────────────────────────────────────

    @Test
    @DisplayName("promptForString returns valid string within length constraints")
    void promptForStringValid() {
        InputResolver resolver = resolverWithInput("Alice\n");
        String result = resolver.promptForString("Name: ", 1, 20);
        assertEquals("Alice", result);
    }

    @Test
    @DisplayName("promptForString returns 'Z' for undo signal")
    void promptForStringUndoZ() {
        InputResolver resolver = resolverWithInput("Z\n");
        String result = resolver.promptForString("Name: ", 1, 20);
        assertEquals("Z", result);
    }

    @Test
    @DisplayName("promptForString returns 'Z' for 'UNDO' signal")
    void promptForStringUndoWord() {
        InputResolver resolver = resolverWithInput("UNDO\n");
        String result = resolver.promptForString("Name: ", 1, 20);
        assertEquals("Z", result);
    }

    @Test
    @DisplayName("promptForString re-prompts on empty input then accepts valid")
    void promptForStringEmptyThenValid() {
        InputResolver resolver = resolverWithInput("\nBob\n");
        String result = resolver.promptForString("Name: ", 1, 20);
        assertEquals("Bob", result);

        String output = capturedOut.toString();
        assertTrue(output.contains("Input cannot be empty"),
                "Should print empty input error");
    }

    @Test
    @DisplayName("promptForString re-prompts when input is too short")
    void promptForStringTooShortThenValid() {
        InputResolver resolver = resolverWithInput("A\nAlice\n");
        String result = resolver.promptForString("Name: ", 3, 20);
        assertEquals("Alice", result);

        String output = capturedOut.toString();
        assertTrue(output.contains("Input too short"),
                "Should print too-short error");
    }

    @Test
    @DisplayName("promptForString re-prompts when input is too long")
    void promptForStringTooLongThenValid() {
        InputResolver resolver = resolverWithInput("ThisNameIsWayTooLong\nBob\n");
        String result = resolver.promptForString("Name: ", 1, 5);
        assertEquals("Bob", result);

        String output = capturedOut.toString();
        assertTrue(output.contains("Input too long"),
                "Should print too-long error");
    }

    @Test
    @DisplayName("promptForString sanitizes control characters from input")
    void promptForStringSanitizesControlChars() {
        // Input with control char (e.g. null byte) — should be stripped
        InputResolver resolver = resolverWithInput("Al\u0000ice\n");
        String result = resolver.promptForString("Name: ", 1, 20);
        assertEquals("Alice", result);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Creates an InputResolver backed by the given simulated input string.
     * Each line of input should be separated by '\n'.
     */
    private InputResolver resolverWithInput(String simulatedInput) {
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8));
        System.setIn(inputStream);
        return new InputResolver();
    }
}
