package org.mjc.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.mjc.antlr.MiniJavaParser;
import org.mjc.antlr.MiniJavaLexer;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.antlr.v4.runtime.CharStreams;

import java.io.InputStream;

@Log4j2
@NoArgsConstructor
public class AntlrParser {
    private final MiniJavaParser parser = new MiniJavaParser(null);

    public boolean validateSyntax(InputStream inputStream) {
        log.info("Validating syntax");
        boolean isValid = true;

        try {
            var charStream = CharStreams.fromStream(inputStream);

            var lexer = new MiniJavaLexer(charStream);
            configureLexer(lexer);

            var tokenStream = new CommonTokenStream(lexer);
            configureParser(tokenStream);

            parser.program();
        } catch (ParseCancellationException e) {
            log.error("Syntax validation failed", e);
            isValid = false;
        } catch (Exception e) {
            log.error("Unexpected error during syntax validation", e);
            isValid = false;
        }

        return isValid;
    }

    private void configureLexer(MiniJavaLexer lexer) {
        lexer.removeErrorListeners();
        lexer.addErrorListener(ParserExceptionListener.INSTANCE);
    }

    private void configureParser(CommonTokenStream tokenStream) {
        parser.setTokenStream(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(ParserExceptionListener.INSTANCE);
    }
}