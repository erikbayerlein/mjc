package org.mjc;

import org.mjc.mjc.MjcCompiler;
import org.mjc.parser.AntlrParser;
import org.mjc.visitor.types.TypeCheckingVisitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("src/test/resources/testFiles/Factorial.java");

        MjcCompiler compiler = MjcCompiler.builder()
                .parser(new AntlrParser())
                .semanticAnalysis(new TypeCheckingVisitor())
                .build();

        compiler.compile(inputStream, System.out);
    }
}