package org.mjc.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AntlrParser Syntax Validation Tests")
class AntlrParserTest {

    @Test
    @DisplayName("Valid input should return true for syntax validation")
    void validateSyntaxWithValidInputReturnsTrue() {
        String validInput = "class Main{ public static void main(String[] a) { }}";
        InputStream inputStream = new ByteArrayInputStream(validInput.getBytes());

        AntlrParser parser = new AntlrParser();
        boolean result = parser.validateSyntax(inputStream);

        assertTrue(result);
    }

    @Test
    @DisplayName("Invalid input should return false for syntax validation")
    void validateSyntaxWithInvalidInputReturnsFalse() {
        String invalidInput = "class Main { public static void main(String[] args) {";
        InputStream inputStream = new ByteArrayInputStream(invalidInput.getBytes());

        AntlrParser parser = new AntlrParser();
        boolean result = parser.validateSyntax(inputStream);

        assertFalse(result);
    }

    @Test
    @DisplayName("Empty input should return false for syntax validation")
    void validateSyntaxWithEmptyInputReturnsFalse() {
        String emptyInput = "";
        InputStream inputStream = new ByteArrayInputStream(emptyInput.getBytes());

        AntlrParser parser = new AntlrParser();
        boolean result = parser.validateSyntax(inputStream);

        assertFalse(result);
    }
}
