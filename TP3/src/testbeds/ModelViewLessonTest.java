package testbeds;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import guiViewLesson.ModelViewLesson;

/**
 * Tests the AI refinement prototype in ModelViewLesson.
 * These tests verify that generateRefinedSuggestion() correctly
 * handles punctuation, blank input, and null input.
 *
 * The tests in this class validate the
 * ModelViewLesson.generateRefinedSuggestion() method.
 */
public class ModelViewLessonTest {

    /**
     * Tests that a period is added when the lesson text
     * does not already end with punctuation.
     */
    @Test
    void testAddsPeriod() {
        String result =
                ModelViewLesson.generateRefinedSuggestion(
                        "testing early can help prevent problems");

        assertEquals(
                "testing early can help prevent problems.",
                result);
    }

    /**
     * Tests that existing ending punctuation is preserved.
     */
    @Test
    void testKeepsExistingPunctuation() {
        String result =
                ModelViewLesson.generateRefinedSuggestion(
                        "Testing early can help prevent problems.");

        assertEquals(
                "Testing early can help prevent problems.",
                result);
    }

    /**
     * Tests that blank lesson text returns an empty string.
     */
    @Test
    void testBlankText() {
        String result =
                ModelViewLesson.generateRefinedSuggestion("   ");

        assertEquals("", result);
    }

    /**
     * Tests that null lesson text returns an empty string.
     */
    @Test
    void testNullText() {
        String result =
                ModelViewLesson.generateRefinedSuggestion(null);

        assertEquals("", result);
    }
}