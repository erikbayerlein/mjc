package org.mjc.mjc;

import org.mjc.ast.Program;
import org.mjc.exceptions.LexicalOrSemanticAnalysisException;

import java.io.InputStream;
import java.util.Optional;

public interface ParserStrategy {
	Optional<Program> getProgram(InputStream stream);

	boolean validateSyntax(InputStream stream);

	Program getProgramOrThrow(InputStream stream) throws LexicalOrSemanticAnalysisException;
}