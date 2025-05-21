package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
public class If extends Statement {
	private Expression condition;
	@Builder.Default
	private Statement thenStmt = new Block(new ArrayList<Statement>());
	@Builder.Default
	private Statement elseStmt = new Block(new ArrayList<Statement>());;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}
