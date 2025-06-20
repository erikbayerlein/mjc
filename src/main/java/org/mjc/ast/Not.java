package org.mjc.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class Not extends Expression {
	private Expression e;

	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}