package org.mjc.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.mjc.antlr.MiniJavaParser;
import org.mjc.antlr.MiniJavaLexer;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.antlr.v4.runtime.CharStreams;
import org.mjc.ast.Program;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Log4j2
@NoArgsConstructor
public class AntlrParser {
    private final MiniJavaParser parser = new MiniJavaParser(null);

    public Optional<Program> getProgram(InputStream stream) {
        log.info("Parsing program");
        try {
            var parser = createAndConfigureParser(stream);

            var gen = new ASTGenerator();
            parser.addParseListener(gen);

            parser.goal();

            return Optional.of(gen.getProgram());
        } catch (ParseCancellationException e) {
            log.error("Parsing program failed due to syntax errors", e);
        } catch (Exception e) {
            log.error("Unexpected error during program parsing", e);
        }

        return Optional.empty();
    }

    public boolean validateSyntax(InputStream inputStream) {
        log.info("Validating syntax");
        boolean isValid = true;

        try {
            var parser = createAndConfigureParser(inputStream);
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

    private MiniJavaParser createAndConfigureParser(InputStream inputStream) throws IOException {
        var charStream = CharStreams.fromStream(inputStream);

        var lexer = new MiniJavaLexer(charStream);
        configureLexer(lexer);

        var tokenStream = new CommonTokenStream(lexer);
        configureParser(tokenStream);

        return parser;
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