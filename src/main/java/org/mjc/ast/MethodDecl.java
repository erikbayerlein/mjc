package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class MethodDecl extends Node {
	private Type returnType;
	private Identifier name;
	private Expression returnExpression;

	@Builder.Default
	private ArrayList<Formal> formals = new ArrayList<>();
	@Builder.Default
	private ArrayList<VarDecl> vars = new ArrayList<>();
	@Builder.Default
	private ArrayList<Statement> statements = new ArrayList<>();

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
