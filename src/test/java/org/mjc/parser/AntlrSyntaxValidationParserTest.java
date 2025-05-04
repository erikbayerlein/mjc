package org.mjc.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AntlrParser Syntax Validation Tests")
class AntlrSyntaxValidationParserTest {

    private AntlrParser parser;

    @BeforeEach
    void setUp() {
        parser = new AntlrParser();
    }

    private void validateSyntaxFromFile(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath));
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        boolean result = parser.validateSyntax(inputStream);

        assertTrue(result, "The program in " + filePath + " should have valid syntax.");
    }

    @Test
    @DisplayName("Valid input should return true for syntax validation")
    void validateSyntaxWithValidInputReturnsTrue() {
        String validInput = "class Main{ public static void main(String[] a) { }}";
        InputStream inputStream = new ByteArrayInputStream(validInput.getBytes());

        boolean result = parser.validateSyntax(inputStream);

        assertTrue(result);
    }

    @Test
    @DisplayName("Invalid input should return false for syntax validation")
    void validateSyntaxWithInvalidInputReturnsFalse() {
        String invalidInput = "class Main { public static void main(String[] args) {";
        InputStream inputStream = new ByteArrayInputStream(invalidInput.getBytes());

        boolean result = parser.validateSyntax(inputStream);

        assertFalse(result);
    }

    @Test
    @DisplayName("Empty input should return false for syntax validation")
    void validateSyntaxWithEmptyInputReturnsFalse() {
        String emptyInput = "";
        InputStream inputStream = new ByteArrayInputStream(emptyInput.getBytes());

        boolean result = parser.validateSyntax(inputStream);

        assertFalse(result);
    }

    @Test
    @DisplayName("BinaryTree program should have valid syntax")
    void validateSyntaxWithBinaryTreeProgram() throws IOException {
        validateSyntaxFromFile("src/test/resources/testFiles/BinaryTree.java");
    }

    @Test
    @DisplayName("BinarySearch program should have valid syntax")
    void validateSyntaxWithBinarySearchProgram() throws IOException {
        validateSyntaxFromFile("src/test/resources/testFiles/BinarySearch.java");
    }

    @Test
    @DisplayName("BubbleSort program should have valid syntax")
    void validateSyntaxWithBubbleSortProgram() throws IOException {
        validateSyntaxFromFile("src/test/resources/testFiles/BubbleSort.java");
    }

    @Test
    @DisplayName("Factorial program should have valid syntax")
    void validateSyntaxWithFactorialProgram() throws IOException {
        validateSyntaxFromFile("src/test/resources/testFiles/Factorial.java");
    }

    @Test
    @DisplayName("LinearSearch program should have valid syntax")
    void validateSyntaxWithLinearSearchProgram() throws IOException {
        validateSyntaxFromFile("src/test/resources/testFiles/LinearSearch.java");
    }

    @Test
    @DisplayName("LinkedList program should have valid syntax")
    void validateSyntaxWithLinkedListProgram() throws IOException {
        validateSyntaxFromFile("src/test/resources/testFiles/LinkedList.java");
    }

    @Test
    @DisplayName("QuickSort program should have valid syntax")
    void validateSyntaxWithQuickSortProgram() throws IOException {
        validateSyntaxFromFile("src/test/resources/testFiles/QuickSort.java");
    }

    @Test
    @DisplayName("TreeVisitor program should have valid syntax")
    void validateSyntaxWithTreeVisitorProgram() throws IOException {
        validateSyntaxFromFile("src/test/resources/testFiles/TreeVisitor.java");
    }
}
