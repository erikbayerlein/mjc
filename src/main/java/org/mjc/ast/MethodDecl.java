package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class MethodDecl extends Node {
	private Type type;
	private String identifier;
	@Builder.Default
	private FormalList formals = new FormalList();
	@Builder.Default
	private VarDeclList varDecls = new VarDeclList();
	@Builder.Default
	private StatementList statements = new StatementList();
	private Expression returnExpression;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
