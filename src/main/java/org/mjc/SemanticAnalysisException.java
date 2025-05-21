package org.mjc;

public class SemanticAnalysisException extends RuntimeException {
	public SemanticAnalysisException(String message) {
		super(message);
	}

	public SemanticAnalysisException(String message, Throwable cause) {
		super(message, cause);
	}
}
