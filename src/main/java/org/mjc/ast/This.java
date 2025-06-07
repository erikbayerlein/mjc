package org.mjc.ast;

import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
public class This extends Expression {
	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}