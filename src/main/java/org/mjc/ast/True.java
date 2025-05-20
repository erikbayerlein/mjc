package org.mjc.ast;

import lombok.EqualsAndHashCode;
import org.mjc.visitor.Visitor;

@EqualsAndHashCode(callSuper = false)
public class True extends Expression {
	public True() {};
	@Override
	public <T> T accept(Visitor<T> v) {
		return v.visit(this);
	}
}