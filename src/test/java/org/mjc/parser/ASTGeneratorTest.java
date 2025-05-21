package org.mjc.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mjc.antlr.MiniJavaLexer;
import org.mjc.antlr.MiniJavaParser;
import org.mjc.ast.Program;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ASTGenerator Tests")
class ASTGeneratorTest {

    private final String testFilePath = "src/test/resources/testFiles/";

    private Program generateAST(String input) {
        MiniJavaLexer lexer = new MiniJavaLexer(CharStreams.fromString(input));
        MiniJavaParser parser = new MiniJavaParser(new CommonTokenStream(lexer));
        ASTGenerator astGenerator = new ASTGenerator();
        parser.addParseListener(astGenerator);
        parser.goal();
        return astGenerator.getProgram();
    }

    private Program generateASTFromFile(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath));
        return generateAST(content);
    }

    @Test
    @DisplayName("Valid input should generate a valid AST")
    void generateASTWithValidInput() {
        String validInput = "class Main { public static void main(String[] a) { } }";
        Program program = generateAST(validInput);
        assertNotNull(program, "The AST should not be null for valid input.");
    }

    @Test
    @DisplayName("BinaryTree program should generate a valid AST")
    void generateASTWithBinaryTreeProgram() throws IOException {
        Program program = generateASTFromFile(testFilePath + "BinaryTree.java");
        assertNotNull(program, "The AST for BinaryTree.java should not be null.");
    }

    @Test
    @DisplayName("BinarySearch program should generate a valid AST")
    void generateASTWithBinarySearchProgram() throws IOException {
        Program program = generateASTFromFile(testFilePath + "BinarySearch.java");
        assertNotNull(program, "The AST for BinarySearch.java should not be null.");
    }

    @Test
    @DisplayName("BubbleSort program should generate a valid AST")
    void generateASTWithBubbleSortProgram() throws IOException {
        Program program = generateASTFromFile(testFilePath + "BubbleSort.java");
        assertNotNull(program, "The AST for BubbleSort.java should not be null.");
    }

    @Test
    @DisplayName("Factorial program should generate a valid AST")
    void generateASTWithFactorialProgram() throws IOException {
        Program program = generateASTFromFile(testFilePath + "Factorial.java");
        assertNotNull(program, "The AST for Factorial.java should not be null.");
    }

    @Test
    @DisplayName("LinearSearch program should generate a valid AST")
    void generateASTWithLinearSearchProgram() throws IOException {
        Program program = generateASTFromFile(testFilePath + "LinearSearch.java");
        assertNotNull(program, "The AST for LinearSearch.java should not be null.");
    }

    @Test
    @DisplayName("LinkedList program should generate a valid AST")
    void generateASTWithLinkedListProgram() throws IOException {
        Program program = generateASTFromFile(testFilePath + "LinkedList.java");
        assertNotNull(program, "The AST for LinkedList.java should not be null.");
    }

    @Test
    @DisplayName("QuickSort program should generate a valid AST")
    void generateASTWithQuickSortProgram() throws IOException {
        Program program = generateASTFromFile(testFilePath + "QuickSort.java");
        assertNotNull(program, "The AST for QuickSort.java should not be null.");
    }

    @Test
    @DisplayName("TreeVisitor program should generate a valid AST")
    void generateASTWithTreeVisitorProgram() throws IOException {
        Program program = generateASTFromFile(testFilePath + "TreeVisitor.java");
        assertNotNull(program, "The AST for TreeVisitor.java should not be null.");
    }
}