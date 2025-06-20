package org.mjc.parser;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.mjc.antlr.MiniJavaLexer;
import org.mjc.antlr.MiniJavaParser;
import org.mjc.ast.Program;
import org.mjc.exceptions.LexicalOrSemanticAnalysisException;

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
			var charStream = CharStreams.fromStream(stream);
			var lexer = new MiniJavaLexer(charStream);
			lexer.removeErrorListeners();
			lexer.addErrorListener(AntlrParserExceptionListener.INSTANCE);

			var tokens = new CommonTokenStream(lexer);

			parser.setTokenStream(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(AntlrParserExceptionListener.INSTANCE);

			var gen = new ASTGenerator();
			parser.addParseListener(gen);
			assert parser.getNumberOfSyntaxErrors() == 0;
			parser.goal();

			return Optional.of(gen.getProgram());

		} catch (Exception e) {
			log.error("Error parsing program", e);
			return Optional.empty();
		}
	}

	public boolean validateSyntax(InputStream stream) {
		log.info("Checking syntax");
		boolean status = true;

		try {
			var charStream = CharStreams.fromStream(stream);
			var lexer = new MiniJavaLexer(charStream);
			lexer.removeErrorListeners();
			lexer.addErrorListener(AntlrParserExceptionListener.INSTANCE);

			var tokens = new CommonTokenStream(lexer);
			parser.setTokenStream(tokens);
			parser.removeErrorListeners();
			parser.addErrorListener(AntlrParserExceptionListener.INSTANCE);
			parser.program();
		} catch (ParseCancellationException e) {
			log.error("Syntax error", e);
			status = false;
		} catch (Exception e) {
			log.error("Error", e);
			status = false;
		}

		return status;
	}

	public Program getProgramOrThrow(InputStream stream) throws LexicalOrSemanticAnalysisException {
		log.info("Parsing program");

		try {
			var charStream = CharStreams.fromStream(stream);
			var lexer = new MiniJavaLexer(charStream);
			lexer.removeErrorListeners();
			lexer.addErrorListener(AntlrParserExceptionListener.INSTANCE);

			var tokens = new CommonTokenStream(lexer);

			var parser = new MiniJavaParser(tokens);

			parser.removeErrorListeners();
			parser.addErrorListener(AntlrParserExceptionListener.INSTANCE);

			var gen = new ASTGenerator();
			parser.addParseListener(gen);
			assert parser.getNumberOfSyntaxErrors() == 0;
			parser.goal();

			return gen.getProgram();

		} catch (IOException e) {
			throw new LexicalOrSemanticAnalysisException("IO Exception", e);
		} catch (Exception e) {
			throw new LexicalOrSemanticAnalysisException("Error parsing program", e);
		}
	}
}
