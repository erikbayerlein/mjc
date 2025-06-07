package org.mjc.mjc;

import org.mjc.ast.Program;
import org.mjc.exceptions.SemanticAnalysisException;
import org.mjc.visitor.symbols.MainTable;

public interface SemanticAnalysisStrategy {
	boolean isSemanticsOk(Program program);

	void isSemanticsOkOrThrow(Program program) throws SemanticAnalysisException;

	MainTable getMainTable();
}
